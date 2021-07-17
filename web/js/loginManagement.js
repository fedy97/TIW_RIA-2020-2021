/**
 * Login management
 */
(function() {
    //Link graphics
    var login_button = document.getElementById("login_button");
    var login_warning_div = document.getElementById('login_warning_id');
    var password_input = register_button.closest("form").querySelector('input[name="password"]');
    var repeat_password_input = register_button.closest("form").querySelector('input[name="passwordConfirm"]');
    var register_warning_div = document.getElementById('register_warning_id');

    //Attach to login button
    login_button.addEventListener("click", (e) => {
        var form = e.target.closest("form"); 
        login_warning_div.style.display = 'none';
        if (form.checkValidity()) { //Do form check
            sendToServer(form, login_warning_div, 'Login');
        }else 
            form.reportValidity(); //If not valid, notify
    });

    function sendToServer(form, error_div, request_url){
        makeCall("POST", request_url, form, function(resp){
            switch(resp.status){ //Get status code
                case 200: //Okay
                    var data = JSON.parse(resp.responseText);
                    sessionStorage.setItem('id', data.id);
                    sessionStorage.setItem('name', data.name);
                    window.location.href = "home.html";
                    break;
                case 400: // bad request
                case 401: // unauthorized
                case 500: // server error
                    error_div.textContent = resp.responseText;
                    error_div.style.display = 'block';
                    break;
                default: //Error
                    error_div.textContent = "Request reported status " + resp.status;
                    error_div.style.display = 'block';
            }
        });
    }
})();

