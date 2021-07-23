(function() {
    //Link graphics
    var login_button = document.getElementById("login_button");
    var login_warning_div = document.getElementById('login_warning_id');

    //Attach to login button
    login_button.addEventListener("click", (e) => {
        var form = e.target.closest("form");
        login_warning_div.style.display = 'none';
        if (form.checkValidity()) { //Do form check
            sendToServer(form, login_warning_div, 'login');
        }else
            form.reportValidity(); //If not valid, notify
    });

    function sendToServer(form, error_div, request_url){
        makeCall("POST", request_url, form, function(resp){
            console.log(resp.status);
            switch(resp.status){ //Get status code
                case 200: //Okay
                    sessionStorage.setItem('username',  JSON.parse(resp.responseText).email);
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

