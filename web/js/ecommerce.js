/**
 * Bank
 */
(function () {
    //Vars
    let menu, articleList, orderList, searchComponent, cartComponent;

    let pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    });

    function PageOrchestrator() {
        this.start = function () {
            //Init components
            menu = new Menu(
                sessionStorage.getItem("username"),
                document.getElementById("user-data"),
                document.getElementById("home_a"),
                document.getElementById("cart_a"),
                document.getElementById("order_a"),
                document.getElementById("logout")
            );

            articleList = new ArticleList(
                document.getElementById("last-articles")
            );

            searchComponent = new Search(
                document.getElementById("search"),
                articleList,
                document.getElementById("search_div")
            );

            orderList = new OrderList(document.getElementById("order_table"));

            this.refresh = function () {
                //Refresh view
                menu.show();
                articleList.show('home');
                orderList.hide();
            };
        }

        function Menu(_email, _user_item, _home, _cart, _order,
                      _logout_button) {

            let self = this;
            this.cart = _cart;
            this.home = _home;
            this.order = _order;
            this.user_item = _user_item;
            this.logout_button = _logout_button;

            this.show = function () {
                self.user_item.textContent = _email;
            }

            this.home.addEventListener("click", e => {
                //TODO hide all other components
                orderList.hide();
                articleList.show('home');
                searchComponent.show();
            });

            this.cart.addEventListener("click", e => {
                articleList.hide();
                searchComponent.hide();
                cartComponent.show();
            });

            this.order.addEventListener("click", e => {
                articleList.hide();
                searchComponent.hide();
                //cartComponent.hide();
                orderList.show();
            });

            this.logout_button.addEventListener("click", e => {
                sessionStorage.clear();
            });
        }

        function Search(_search_button, _article_list_component, _search_component) {
            let self = this;
            this.search_button = _search_button;
            this.article_list_component = _article_list_component;
            this.search_component = _search_component;

            this.search_button.addEventListener("click", (e) => {

                let search_form = e.target.closest("form");
                if (search_form.checkValidity()) {
                    let hint = search_form.querySelector("input[name='hint']");
                    self.article_list_component.show('search?hint=' + hint.value);
                } else {
                    search_form.reportValidity();
                }
            })

            this.show = function () {
                self.search_component.style.display = "block";
            }

            this.hide = function () {
                self.search_component.style.display = "none";
            }
        }

        function OrderList(
            _order_table) {
            console.log(_order_table);
            let self = this;
            this.order_table = _order_table;

            this.show = function () {
                let title = document.getElementById("home_title");
                title.textContent = "Orders";
                //Request and update with the results
                makeCall("GET", 'order', null, (resp) => {
                    switch (resp.status) {
                        case 200: //ok
                            let orders = JSON.parse(resp.responseText);
                            self.update(orders);
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

            this.update = function (_orders) {

                self.order_table.style.display = "none";
                self.order_table.innerHTML = "";

                if (_orders.length === 0) {
                    self.order_table.textContent = "No orders yet";
                    self.order_table.style.display = "block";
                } else {
                    _orders.forEach((order) => {
                        let table = document.createElement("table");
                        table.className = "blue-div";
                        let thead = document.createElement("thead");
                        table.appendChild(thead);
                        let tr = document.createElement("tr");
                        table.appendChild(tr);
                        let th1 = document.createElement("th");
                        th1.textContent = "Seller Name";
                        let th2 = document.createElement("th");
                        th2.textContent = "Price Articles";
                        let th3 = document.createElement("th");
                        th3.textContent = "Price Shipment";
                        let th4 = document.createElement("th");
                        th4.textContent = "Total Price";
                        let th5 = document.createElement("th");
                        th5.textContent = "Order Date";
                        let th6 = document.createElement("th");
                        th6.textContent = "Shipment Date";
                        let th7 = document.createElement("th");
                        th7.textContent = "Shipment Address";
                        tr.appendChild(th1);
                        tr.appendChild(th2);
                        tr.appendChild(th3);
                        tr.appendChild(th4);
                        tr.appendChild(th5);
                        tr.appendChild(th6);
                        tr.appendChild(th7);
                        table.appendChild(document.createElement("tbody"));
                        let tr2 = document.createElement("tr");
                        table.appendChild(tr2);
                        let td1 = document.createElement("td");
                        td1.textContent = order.sellerName;
                        let td2 = document.createElement("td");
                        td2.textContent = order.priceArticles + " €";
                        let td3 = document.createElement("td");
                        td3.textContent = order.priceShipment + " €";
                        let td4 = document.createElement("td");
                        td4.textContent = order.priceTotal + " €";
                        let td5 = document.createElement("td");
                        td5.textContent = order.orderDate;
                        let td6 = document.createElement("td");
                        td6.textContent = order.shipmentDate;
                        let td7 = document.createElement("td");
                        td7.textContent = order.shipmentAddr;
                        tr2.appendChild(td1);
                        tr2.appendChild(td2);
                        tr2.appendChild(td3);
                        tr2.appendChild(td4);
                        tr2.appendChild(td5);
                        tr2.appendChild(td6);
                        tr2.appendChild(td7);
                        for (let i = 0; i < order.articleBeans.length; i++) {
                            let articles = order.articleBeans;
                            let th8 = document.createElement("th");
                            th8.textContent = "Article Name";
                            let th9 = document.createElement("th");
                            th9.textContent = "Article Qty";
                            let th10 = document.createElement("th");
                            th10.textContent = "Article Price";
                            table.appendChild(th8);
                            table.appendChild(th9);
                            table.appendChild(th10);
                            let tr3 = document.createElement("tr");
                            let td8 = document.createElement("td");
                            td8.textContent = articles[i].name;
                            let td9 = document.createElement("td");
                            td9.textContent = articles[i].quantity;
                            let td10 = document.createElement("td");
                            td10.textContent = articles[i].price + " €";
                            table.appendChild(tr3);
                            tr3.appendChild(td8);
                            tr3.appendChild(td9);
                            tr3.appendChild(td10);
                        }
                        self.order_table.appendChild(table);
                        self.order_table.appendChild(document.createElement("br"));
                        self.order_table.appendChild(document.createElement("br"));
                    });
                    self.order_table.style.display = "block";
                }

            };

            this.hide = function () {
                self.order_table.style.display = "none";
            }
        }


        function ArticleList(
            _article_div) {

            let self = this;
            this.article_list = _article_div;

            this.show = function (source) {
                let title = document.getElementById("home_title");
                title.textContent = "Home";
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

            this.hide = function () {
                self.article_list.style.display = "none";
            }
        }


    }
})();
