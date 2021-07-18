/**
 * Bank
 */
(function () {
    //Vars
    let userInfo, articleList, searchComponent;

    let pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    });

    function PageOrchestrator() {
        this.start = function () {
            //Init components
            userInfo = new UserInfo(
                sessionStorage.getItem('name'),
                sessionStorage.getItem('id'),
                document.getElementById("user-data"),
                document.getElementById("logout")
            );

            articleList = new ArticleList(
                document.getElementById("last-articles")
            );

            searchComponent = new Search(document.getElementById("search"), articleList);

            //     sellerOfferList = new TransferList(
            //         document.getElementById("account-details"),
            //         document.getElementById("account-name"),
            //         document.getElementById("account-code"),
            //         document.getElementById("account-balance"),
            //         document.getElementById("create-transfer-form"),
            //         document.getElementById("transfer-form-button"),
            //         document.getElementById("create-transfer-button"),
            //         document.getElementById("transfers"),
            //         document.getElementById("transfers-message")
            //     );
            //
            //     addressBook = new AddressBook(
            //         document.getElementById("add-contact"),
            //         document.getElementById("dest-owner-code"),
            //         document.getElementById("dest-account-code"),
            //         document.getElementById("add-contact-warning"),
            //         document.getElementById("add-contact-status-loading"),
            //         document.getElementById("add-contact-status-ok"),
            //         document.getElementById("add-contact-status-ko"),
            //         document.getElementById("create-transfer-warning"),
            //         document.getElementById("dest-ids-datalist"),
            //         document.getElementById("dest-accounts-datalist")
            //     );
            // };
            this.refresh = function () {
                //Refresh view
                // userInfo.show();
                articleList.show('home');
            };
        }

        function UserInfo(
            _name, _id, _user_item,
            _logout_button) {

            this.name = _name;
            this.user_item = _user_item;
            this.logout_button = _logout_button;

            this.show = function () {
                self.user_item.textContent = self.name;
            }

            this.logout_button.addEventListener("click", e => {
                sessionStorage.clear();
            });
        }

        function Search(_search_button, _article_list_component) {
            let self = this;
            this.search_button = _search_button;
            this.article_list_component = _article_list_component;

            this.search_button.addEventListener("click", (e) => {

                let search_form = e.target.closest("form");
                if (search_form.checkValidity()) {
                    let hint = search_form.querySelector("input[name='hint']");
                    self.article_list_component.show('search?hint=' + hint.value);
                } else {
                    search_form.reportValidity();
                }
            })

        }


        function ArticleList(
            _article_div) {

            let self = this;
            this.article_list = _article_div;

            this.show = function (source) {
                //Request and update with the results
                makeCall("GET", source, null, (resp) => {
                    switch (resp.status) {
                        case 200: //ok
                            let articles = JSON.parse(resp.responseText);
                            self.update(articles, null);
                            break;
                        case 400: // bad request
                        case 401: // unauthorized
                        case 500: // server error
                            self.update(null, resp.responseText);
                            break;
                        default: //Error
                            self.update(null, "Request reported status " + resp.status);
                            break;
                    }
                });
            };

            this.update = function (_articles, _error) {

                self.article_list.style.display = "none";
                self.article_list.innerHTML = "";

                if (_error) {

                    self.article_list.textContent = _error;
                    self.article_list.style.display = "block";

                } else {

                    if (_articles.length === 0) {
                        self.article_list.textContent = "No items";
                        self.article_list.style.display = "block";
                    } else {
                        let item, item_title, item_data, img, div1, div2, b1, b2, details_button;
                        _articles.forEach((art) => {
                            item = document.createElement("div");
                            item.className = "item item-blue";
                            item_title = document.createElement("div");
                            item_title.className = "item-title";
                            item_title.textContent = art.name;
                            item.appendChild(item_title);

                            img = document.createElement("img");
                            img.setAttribute("src", art.photo);
                            item.appendChild(img);

                            item_data = document.createElement("div");
                            item_data.className = "item-data";

                            b1 = document.createElement("b");
                            b1.textContent = "Description: ";
                            div1 = document.createElement("div");
                            div1.appendChild(b1);
                            div1.appendChild(document.createTextNode(art.description));
                            item_data.appendChild(div1);


                            b2 = document.createElement("b");
                            b2.textContent = "Category: ";
                            div2 = document.createElement("div");
                            div2.appendChild(b2);
                            div2.appendChild(document.createTextNode(art.description));
                            item_data.appendChild(div2);

                            item.appendChild(item_data);

                            details_button = document.createElement("a");
                            details_button.className = "btn btn-blue btn-small btn-primary";
                            details_button.textContent = "See details";

                            details_button.setAttribute('article_id', art.id);
                            details_button.addEventListener("click", (e) => {
                                // if(e.target.textContent === "Open"){
                                //     if(self.last_used_open_button !== null){
                                //         self.last_used_open_button.textContent = "Open";
                                //     }
                                //     e.target.textContent = "Hide";
                                //     self.last_used_open_button = e.target;
                                //     self.currentSelectedId = acc.id;
                                //     transferList.show(acc.id);
                                // }else{
                                //     self.last_used_open_button = null;
                                //     self.currentSelectedId = NaN;
                                //     e.target.textContent = "Open";
                                //     transferList.hide();
                                // }
                            });
                            item.appendChild(details_button);

                            self.article_list.appendChild(item);
                        });
                        self.article_list.style.display = "block";
                    }
                }
            };
        }


    }
})();
