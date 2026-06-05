describe('DOM Check', () => {
  it('Should not have any React console errors on the dashboard', () => {
    let consoleErrors: string[] = [];
    
    Cypress.on('window:before:load', (win) => {
      cy.spy(win.console, 'error').as('consoleError');
      win.console.error = (...args: any[]) => {
        const msg = args.join(' ');
        if (msg.includes('Warning: validateDOMNesting') || msg.includes('Warning: Invalid DOM property') || msg.includes('Warning: Each child in a list')) {
          consoleErrors.push(msg);
        }
      };
    });

    cy.visit('http://localhost:5173');
    
    // login
    cy.get('input[type="email"]').type('demo@finsight.com');
    cy.get('input[type="password"]').type('Demo@1234');
    cy.get('button[type="submit"]').click();
    
    cy.url().should('include', '/dashboard');
    cy.wait(2000);
    
    // Visit other pages
    cy.visit('http://localhost:5173/transactions');
    cy.wait(1000);
    
    cy.visit('http://localhost:5173/analytics');
    cy.wait(1000);

    cy.then(() => {
      if (consoleErrors.length > 0) {
        throw new Error('Found DOM Errors: ' + consoleErrors.join('\n'));
      }
    });
  });
});
