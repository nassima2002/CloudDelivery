document.addEventListener('DOMContentLoaded', function() {
    // User dropdown toggle
    const userMenuButton = document.getElementById('user-menu-button');
    const userDropdown = document.getElementById('user-dropdown');

    if (userMenuButton && userDropdown) {
        userMenuButton.addEventListener('click', function(event) {
            // Empêcher la propagation de l'événement
            event.stopPropagation();

            // Toggle du dropdown
            userDropdown.classList.toggle('hidden');
            userMenuButton.setAttribute('aria-expanded', userDropdown.classList.contains('hidden') ? 'false' : 'true');
        });

        // Fermer le dropdown quand on clique ailleurs
        document.addEventListener('click', function(event) {
            if (!userMenuButton.contains(event.target) && !userDropdown.contains(event.target)) {
                userDropdown.classList.add('hidden');
                userMenuButton.setAttribute('aria-expanded', 'false');
            }
        });
    }

    // Mobile menu toggle
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');

    if (mobileMenuButton && mobileMenu) {
        mobileMenuButton.addEventListener('click', function() {
            mobileMenu.classList.toggle('hidden');
            mobileMenuButton.setAttribute('aria-expanded', mobileMenu.classList.contains('hidden') ? 'false' : 'true');
        });
    }

    console.log('Menu script initialized');
});