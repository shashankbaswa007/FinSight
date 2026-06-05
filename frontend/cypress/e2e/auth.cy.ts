describe('Authentication Flow', () => {
  beforeEach(() => {
    cy.visit('http://localhost:5173/login');
  });

  it('should display login page', () => {
    cy.get('h1').should('contain', 'FinSight');
    cy.get('input[type="email"]').should('exist');
    cy.get('input[type="password"]').should('exist');
  });

  it('should show error on invalid credentials', () => {
    cy.get('input[type="email"]').type('wrong@example.com');
    cy.get('input[type="password"]').type('wrongpassword');
    cy.get('button[type="submit"]').click();
    
    // Assumes an error message appears
    cy.get('.text-red-600').should('exist');
  });

  it('should login and redirect to dashboard', () => {
    // Uses the demo user seeded by DemoDataSeeder
    cy.get('input[type="email"]').type('demo@finsight.local');
    cy.get('input[type="password"]').type('password123');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/dashboard');
    cy.get('h1').should('contain', 'Dashboard');
    
    // Verify AI Widget is present
    cy.get('button[aria-label="Open AI Advisor"]').should('exist');
  });
});
