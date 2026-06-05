terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ──── Variables ────
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "db_password" {
  description = "RDS root user password"
  type        = string
  sensitive   = true
}

# ──── VPC & Networking ────
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "finsight-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["us-east-1a", "us-east-1b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  tags = {
    Environment = "Production"
    Project     = "FinSight"
  }
}

# ──── RDS MySQL Database ────
resource "aws_db_instance" "mysql" {
  identifier           = "finsight-db"
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro" # Free tier eligible
  allocated_storage    = 20
  
  db_name              = "finsight"
  username             = "finsight_admin"
  password             = var.db_password
  
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.default.name
  
  skip_final_snapshot  = true
}

resource "aws_db_subnet_group" "default" {
  name       = "finsight-main"
  subnet_ids = module.vpc.private_subnets
}

resource "aws_security_group" "db_sg" {
  name        = "finsight-db-sg"
  description = "Allow inbound traffic from ECS"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "MySQL from ECS"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = module.vpc.private_subnets_cidr_blocks
  }
}

# ──── ECS Cluster (Fargate) ────
resource "aws_ecs_cluster" "main" {
  name = "finsight-cluster"
}

resource "aws_ecs_task_definition" "backend" {
  family                   = "finsight-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"

  container_definitions = jsonencode([
    {
      name      = "finsight-api"
      image     = "YOUR_DOCKERHUB_USERNAME/finsight-backend:latest"
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:mysql://${aws_db_instance.mysql.endpoint}/finsight" },
        { name = "SPRING_DATASOURCE_USERNAME", value = "finsight_admin" }
      ]
      secrets = [
        { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "arn:aws:ssm:us-east-1:1234567890:parameter/db_password" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/finsight-backend"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}
