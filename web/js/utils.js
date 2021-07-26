
//let loadingModal = new LoadingModal(document.getElementById("loading_msg"));

function LoadingModal(loading_msg) {
    this.loading_msg = loading_msg;
    this.show = function (message) {
        this.update(message);
        if (!document.body.className.includes("loading"))
            document.body.className += " loading";
    };
    this.update = function (message) {
        if (message) //If a message is supplied to method
            this.loading_msg.textContent = message;
        else
            this.loading_msg.textContent = "Communicating with Server ...";

    };
    this.hide = function () {
        document.body.className = document.body.className.replace(" loading", "");
    };
}

function makeCall(method, relativeUrl, form, done_callback, reset = true) {
    let req = new XMLHttpRequest(); //Create new request
    //Init request
    req.onreadystatechange = function () {
        switch (req.readyState) {
            case XMLHttpRequest.UNSENT:
                //loadingModal.update("Connecting to Server ...");
                break;
            case XMLHttpRequest.OPENED:
                //loadingModal.update("Connected to Server ...");
                break;
            case XMLHttpRequest.HEADERS_RECEIVED:
            case XMLHttpRequest.LOADING:
                //loadingModal.update("Waiting for response ...");
                break;
            case XMLHttpRequest.DONE:
                //loadingModal.update("Request completed");
                if (req.status === 401 && !window.location.href.endsWith("login.html")) //Unauthorized
                    window.location.href = "login.html";
                done_callback(req);

                setTimeout(function () {
                    //loadingModal.hide();
                }, 500);
                break;
        }
    };

    //loadingModal.show(); //Start loading
    //Open request
    req.open(method, relativeUrl, true);
    //Send request
    console.log(form);
    if (form == null) {
        req.send();
    } else if (form instanceof FormData) {
        req.send(form);
    } else {
        console.log(new FormData(form));
        req.send(new FormData(form)); //Send serialized form
    }

    if (form !== undefined && form !== null && !(form instanceof FormData) && reset === true) {
        form.reset(); //Do not touch hidden fields, and restore default values if any
    }
}

function toCurrencyFormat(number) {
    return parseFloat(number).toFixed(2);
}
