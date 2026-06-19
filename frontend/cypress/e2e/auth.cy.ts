describe('Authentication Flow', () => {
  beforeEach(() => {
    cy.visit('http://localhost:5173/login');
  });

  it('should display login page', () => {
    cy.contains('FinSight');
    cy.get('input[type="email"]').should('exist');
    cy.get('input[type="password"]').should('exist');
  });

  it('should show error on invalid credentials', () => {
    cy.get('input[type="email"]').type('wrong@example.com');
    cy.get('input[type="password"]').type('wrongpassword');
    cy.get('button[type="submit"]').click();
    
    // Assumes an error message appears
    cy.get('[data-testid="error-message"]').should('exist');
  });

  it('should login and redirect to dashboard', () => {
    // Uses the demo user seeded by DemoDataSeeder
    cy.get('input[type="email"]').type('demo@finsight.com');
    cy.get('input[type="password"]').type('Demo@1234');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/dashboard');
    cy.get('h1').should('contain', 'Dashboard');
    
    // Verify AI Widget is present
    cy.get('button[aria-label="Open AI Advisor"]').should('exist');
  });
});
