(function () {
    //Vars
    let menu, articleList, articleDetails, orderList, searchComponent, cartComponent;

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

            articleDetails = [];
            orderList = new OrderList(document.getElementById("order_table"));

            cartComponent = new Cart(
                document.getElementById("cart"),
                document.getElementById("sellers_list"),
                document.getElementById("cart_title")
            );

            this.refresh = function () {
                //Refresh view
                menu.show();
                articleList.show('home');
                orderList.hide();
                cartComponent.hide();
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
                cartComponent.hide();
                orderList.hide();
                articleList.show('home');
                searchComponent.show();
            });

            this.cart.addEventListener("click", e => {
                articleList.hide();
                searchComponent.hide();
                orderList.hide();
                cartComponent.show();
            });

            this.order.addEventListener("click", e => {
                articleList.hide();
                searchComponent.hide();
                cartComponent.hide();
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
                        th1.id = order.id;
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
                        //popupWindow(th1.id, table);
                    });
                    self.order_table.style.display = "block";
                }

            };

            this.hide = function () {
                self.order_table.style.display = "none";
            }
        }


        function ArticleList(
            _articles_div) {

            let self = this;
            this.article_list = _articles_div;

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
                        let item, item_title, item_data, img, div1, div2, b1, b2, details_button, details_div;
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
                            b1.textContent = "Code: ";
                            div1 = document.createElement("div");
                            div1.appendChild(b1);
                            div1.appendChild(document.createTextNode(art.id));
                            item_data.appendChild(div1);

                            b2 = document.createElement("b");
                            b2.textContent = "Min price: ";
                            div2 = document.createElement("div");
                            div2.appendChild(b2);
                            div2.appendChild(document.createTextNode(art.price));
                            div2.appendChild(document.createTextNode("€"));
                            item_data.appendChild(div2);

                            item.appendChild(item_data);

                            details_button = document.createElement("a");
                            details_button.className = "btn btn-blue btn-small btn-primary";
                            details_button.textContent = "See details";

                            details_button.setAttribute('article_id', art.id);
                            details_button.addEventListener("click", (e) => {
                                if (e.target.textContent === "See details") {
                                    articleDetails[art.id].show();
                                    e.target.textContent = "Hide";
                                } else {
                                    e.target.textContent = "See details";
                                    articleDetails[art.id].hide();
                                }
                            });
                            item.appendChild(details_button);

                            details_div = document.createElement("div");
                            details_div.style.display = "none";
                            details_div.id = "article_" + art.id;
                            item.appendChild(details_div);

                            articleDetails[art.id] = new ArticleDetails(art.id, details_div);

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


        function ArticleDetails(_article_id,
                                _article_div) {

            let self = this;
            this.article_id = _article_id;
            this.article_div = _article_div;

            this.show = function () {
                //Request and update with the results
                makeCall("GET", "article?article_id=" + self.article_id, null, (resp) => {
                    switch (resp.status) {
                        case 200: //ok
                            let article = JSON.parse(resp.responseText);
                            self.update(article, null);
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

            this.update = function (_article, _error) {

                self.article_div.style.display = "none";

                if (_error) {

                    self.article_list.textContent = _error;
                    self.article_list.style.display = "block";

                } else {
                    let article_data = document.createElement("div");
                    article_data.className = "item-data";

                    let b1 = document.createElement("b");
                    b1.textContent = "Description: ";
                    let div1 = document.createElement("div");
                    div1.appendChild(b1);
                    div1.appendChild(document.createTextNode(_article.description));
                    article_data.appendChild(div1);


                    let b2 = document.createElement("b");
                    b2.textContent = "Category: ";
                    let div2 = document.createElement("div");
                    div2.appendChild(b2);
                    div2.appendChild(document.createTextNode(_article.category));
                    article_data.appendChild(div2);

                    self.article_div.appendChild(article_data);
                    self.article_div.appendChild(document.createElement("br"));

                    let item, item_data, seller_name, seller_price, seller_rating, seller_threshold, add_form,
                        seller_items;
                    _article.sellers.forEach((seller) => {
                        item = document.createElement("div");
                        item.className = "item item-blue";
                        item_data = document.createElement("div");
                        item_data.className = "item-data";

                        seller_name = document.createElement("div");
                        seller_name.id = seller.sellerName + _article.name;
                        seller_name.innerHTML = "<div><b> Seller: </b><span>" + seller.sellerName + "</span></div>";
                        item_data.appendChild(seller_name);


                        seller_price = document.createElement("div");
                        seller_price.innerHTML = "<div><b> Price: </b><span>" + seller.price + "</span><span>&#8364;</span></div>";
                        item_data.appendChild(seller_price);

                        seller_rating = document.createElement("div");
                        seller_rating.innerHTML = "<div><b> Rating: </b><span>" + seller.sellerRating + "</span></div>";
                        item_data.appendChild(seller_rating);

                        item.appendChild(item_data);
                        item.appendChild(document.createElement("br"));

                        seller.shippingPolicies.forEach((shipping_policy) => {
                            let ship_item = document.createElement("div");
                            let span_item = document.createElement("span");

                            if (shipping_policy.maxItem !== null && shipping_policy.maxItem !== undefined) {
                                span_item.textContent = "For order with articles between " + shipping_policy.minItem + " and " + shipping_policy.maxItem + " the shipping cost is " + shipping_policy.shipCost;
                            } else {
                                span_item.textContent = "For more than " + shipping_policy.minItem + " articles the shipping is free";
                            }
                            ship_item.appendChild(span_item);
                            ship_item.appendChild(document.createElement("hr"));
                            item.appendChild(ship_item);
                        });

                        seller_threshold = document.createElement("div");
                        seller_threshold.innerHTML = "Free shipping for orders greater than <b>" + seller.priceThreshold + "</b><b>&#8364;</b>";
                        item.appendChild(seller_threshold);

                        seller_items = document.createElement("div");
                        seller_items.id = "seller_cart" + seller.sellerId + _article.id;
                        let cart = localStorage.getItem("cart_" + sessionStorage.getItem("username"));
                        if (cart === null) cart = {};
                        else
                            cart = JSON.parse(cart);
                        let message;
                        if (cart[seller.sellerId] === null || cart[seller.sellerId] === undefined)
                            message = "You do not have articles of this seller in your cart yet";
                        else {
                            let articles = cart[seller.sellerId].articles;
                            message = "You have already " + articles.length + " items of this seller in the cart";
                        }
                        seller_items.innerHTML = message;
                        item.appendChild(seller_items);

                        add_form = document.createElement("form");
                        add_form.className = "add-article-form";
                        add_form.method = "POST";

                        let article_input = document.createElement("input");
                        article_input.name = "article" + _article.id;
                        article_input.id = "article" + _article.id + seller.sellerId;
                        article_input.type = "hidden";
                        article_input.value = _article.id;
                        article_input.required = true;
                        add_form.appendChild(article_input);


                        let seller_input = document.createElement("input");
                        seller_input.name = "seller_id";
                        seller_input.type = "hidden";
                        seller_input.value = seller.sellerId;
                        seller_input.required = true;
                        add_form.appendChild(seller_input);

                        let form_group = document.createElement("div");
                        form_group.className = "form-group";
                        form_group.innerHTML = '<div class="form-group"><label for="article_qty">Qty</label><input id="qty' + _article.id + seller.sellerId + '" type="number" name="qty' + _article.id + '" value="1" min="1" step="1" required/></div>';
                        add_form.appendChild(form_group);

                        let input_button = document.createElement("input");
                        input_button.className = "btn btn-large btn-blue btn-primary";
                        input_button.type = "button";
                        input_button.value = "Add";

                        input_button.addEventListener("click", (e) => {
                                let cart, seller_entry, article_entry;
                                //let input_seller = add_form.querySelector("input[name='seller_id']");
                                let input_article = document.getElementById("article" + _article.id + seller.sellerId);
                                let input_qty = document.getElementById("qty" + _article.id + seller.sellerId);
                                if (add_form.checkValidity()) {
                                    cart = JSON.parse(localStorage.getItem("cart_" + sessionStorage.getItem("username")));
                                    if (cart !== null && cart !== undefined && cart.length !== 0) {
                                        seller_entry = cart[seller.sellerId];
                                        if (seller_entry !== null && seller_entry !== undefined && seller_entry.length !== 0) {
                                            article_entry = seller_entry.articles.filter(article => article.id === input_article.value);
                                            if (article_entry.length !== 0) {
                                                console.log("adding qty to existing article");
                                                article_entry[0].quantity = (parseInt(article_entry[0].quantity) + parseInt(input_qty.value)).toString();
                                            } else {
                                                console.log("new article in cart")
                                                seller_entry.articles.push({
                                                    quantity: input_qty.value,
                                                    id: input_article.value,
                                                    name: _article.name
                                                });
                                            }
                                        } else {
                                            console.log("empty seller");
                                            cart[seller.sellerId] = {};
                                            cart[seller.sellerId].articles = [];
                                            cart[seller.sellerId].articles.push({
                                                id: input_article.value,
                                                quantity: input_qty.value,
                                                name: _article.name
                                            });
                                        }
                                    } else {
                                        console.log("empty cart");
                                        cart = {};
                                        cart[seller.sellerId] = {};
                                        cart[seller.sellerId].articles = [];
                                        cart[seller.sellerId].articles.push({
                                            id: input_article.value,
                                            quantity: input_qty.value,
                                            name: _article.name
                                        });
                                    }

                                    localStorage.setItem("cart_" + sessionStorage.getItem("username"), JSON.stringify(cart));
                                }
                                menu.cart.dispatchEvent(new Event("click"));
                            }
                            //simulate click on cart
                        );

                        add_form.appendChild(input_button);
                        item.appendChild(add_form);
                        self.article_div.appendChild(item);
                        popupWindow("seller_cart" + seller.sellerId + _article.id, seller_items, seller.sellerId);
                    });
                    self.article_div.style.display = "block";

                }
            };

            this.hide = function () {
                self.article_div.style.display = "none";
                self.article_div.innerHTML = "";
            }
        }
    }

    function Cart(
        _cart_div, _sellers_div, _cart_title) {

        let self = this;
        this.cart_div = _cart_div;
        this.sellers_div = _sellers_div;
        this.cart_title = _cart_title;

        this.show = function () {
            //Request and update with the results
            let shipping_price = 0;
            let articles_price = 0.0;
            let cart = localStorage.getItem("cart_" + sessionStorage.getItem("username"));
            if (cart === null || cart === undefined)
                cart = {};
            else
                cart = JSON.parse(cart);

            for (let seller in cart) {
                if (Object.prototype.hasOwnProperty.call(cart, seller)) {
                    let articles = 0;
                    cart[seller].articles.forEach(entry => {
                        articles += parseInt(entry.quantity);

                        //get article price from BE
                        makeCall("GET", "/price?article_id=" + entry.id + "&seller_id=" + seller, null, (resp) => {
                            if (resp.status === 200) {
                                entry.price = JSON.parse(resp.responseText).price;
                                articles_price += parseFloat(entry.price) * parseFloat(entry.quantity);
                                cart[seller].articles_price = articles_price.toFixed(2);
                            } else {
                                self.update(null, "Request reported status " + resp.status);
                            }
                        });
                    });

                    //get article price from BE
                    makeCall("GET", "shipping?seller_id=" + seller + "&qty=" + articles, null, (resp) => {
                        if (resp.status === 200) {
                            shipping_price = JSON.parse(resp.responseText).shipCost;
                            cart[seller].shipping_price = shipping_price;
                        } else {
                            self.update(null, "Request reported status " + resp.status);
                        }
                    });//get details
                }
            }
            setTimeout(() => {
                self.update(cart);
            }, 500);


        };

        this.update = function (_cart, _error) {

            self.cart_div.style.display = "none";
            self.sellers_div.innerHTML = "";

            if (_error) {
                self.cart_div.textContent = _error;
                self.cart_div.style.display = "block";
            } else {
                let item_title;

                if (Object.keys(_cart).length === 0) {
                    self.cart_title.textContent = "No articles in your cart yet. Go shopping!";
                    self.cart_div.style.display = "block";
                } else {
                    self.cart_title.textContent = "Here you can find your cart articles";

                    for (let seller in _cart) {
                        if (Object.prototype.hasOwnProperty.call(_cart, seller)) {

                            let div1, div2, b1, b2, div3, b3, item_data, item, order_form;

                            item = document.createElement("div");
                            item.className = "item item-blue";
                            item_title = document.createElement("div");
                            item_title.className = "item-title";
                            item_title.textContent = _cart[seller].name;
                            item.appendChild(item_title);

                            _cart[seller].articles.forEach(art => {
                                item_data = document.createElement("div");
                                item_data.className = "item-data";

                                b1 = document.createElement("b");
                                b1.textContent = "Name: ";
                                div1 = document.createElement("div");
                                div1.appendChild(b1);
                                div1.appendChild(document.createTextNode(art.name));
                                item_data.appendChild(div1);

                                b2 = document.createElement("b");
                                b2.textContent = "Price: ";
                                div2 = document.createElement("div");
                                div2.appendChild(b2);
                                div2.appendChild(document.createTextNode(art.price));
                                div2.appendChild(document.createTextNode("€"));
                                item_data.appendChild(div2);

                                b3 = document.createElement("b");
                                b3.textContent = "Qty: ";
                                div3 = document.createElement("div");
                                div3.appendChild(b3);
                                div3.appendChild(document.createTextNode(art.quantity));
                                item_data.appendChild(div3);


                                item_data.appendChild(document.createElement("hr"));
                                item.appendChild(item_data);
                            });

                            item_data = document.createElement("div");
                            item_data.className = "item-data";

                            b1 = document.createElement("b");
                            b1.textContent = "Articles cost: ";
                            div1 = document.createElement("div");
                            div1.appendChild(b1);
                            div1.appendChild(document.createTextNode(_cart[seller].articles_price));
                            div1.appendChild(document.createTextNode("€"));
                            item_data.appendChild(div1);

                            b2 = document.createElement("b");
                            b2.textContent = "Shipping cost: ";
                            div2 = document.createElement("div");
                            div2.appendChild(b2);
                            div2.appendChild(document.createTextNode(_cart[seller].shipping_price));
                            div2.appendChild(document.createTextNode("€"));
                            item_data.appendChild(div2);

                            item.append(item_data);
                            item.append(document.createElement("br"));

                            order_form = document.createElement("form");
                            order_form.className = "order-form";
                            order_form.method = "POST";

                            let seller_input = document.createElement("input");
                            seller_input.name = "seller_id";
                            seller_input.type = "hidden";
                            seller_input.value = seller;
                            seller_input.required = true;
                            order_form.appendChild(seller_input);

                            let articles_input = document.createElement("input");
                            articles_input.name = "articles";
                            articles_input.type = "hidden";
                            articles_input.value = JSON.stringify(_cart[seller].articles);
                            articles_input.required = true;
                            order_form.appendChild(articles_input);

                            let input_button = document.createElement("input");
                            input_button.className = "btn btn-large btn-blue btn-primary";
                            input_button.type = "button";
                            input_button.value = "Order";
                            input_button.addEventListener("click", (e) => {
                                var data = new FormData();
                                data.append("seller_id", seller);
                                data.append("articles", articles_input.value);
                                console.log("articles: " + articles_input.value);
                                console.log("seller: " + seller);
                                makeCall("POST", "order", data, (resp) => {
                                    if (resp.status === 200) {
                                        articleList.hide();
                                        searchComponent.hide();
                                        cartComponent.hide();
                                        let cart = localStorage.getItem("cart_" + sessionStorage.getItem("username"));
                                        let map = JSON.parse(cart);
                                        delete map[seller];
                                        localStorage.setItem("cart_" + sessionStorage.getItem("username"), JSON.stringify(map));
                                        orderList.show();
                                    } else {
                                        self.update(null, "Request reported status " + resp.status);
                                    }
                                });
                            });

                            order_form.appendChild(input_button);
                            item.appendChild(order_form);

                            self.sellers_div.appendChild(item);
                        }
                    }
                    self.cart_div.style.display = "block";
                }
            }
        };

        this.hide = function () {
            self.cart_div.style.display = "none";
        }
    }


    function popupWindow(hoverableElementId, popupTagPosition, sellerId) {
        let popup = {
            open: function () {
                if (this.element == null) {
                    this.element = document.createElement('div');
                    this.element.id = "popup" + hoverableElementId;
                    this.element.className = "blue-div";
                }
                this.element.innerHTML = "";
                // show products in cart of the seller hovered
                let cart = JSON.parse(localStorage.getItem("cart_" + sessionStorage.getItem("username")));
                if (cart !== null && cart !== undefined && cart.length !== 0) {
                    let seller_entry = cart[sellerId];
                    if (seller_entry !== null && seller_entry !== undefined && seller_entry.length !== 0) {
                        let articles = seller_entry.articles;
                        articles.forEach((article) => {
                            let item_data = document.createElement("div");
                            item_data.className = "item-data";

                            let b1 = document.createElement("b");
                            b1.textContent = "Name: ";
                            let div1 = document.createElement("div");
                            div1.appendChild(b1);
                            div1.appendChild(document.createTextNode(article.name));
                            item_data.appendChild(div1);

                            let b3 = document.createElement("b");
                            b3.textContent = "Qty: ";
                            let div3 = document.createElement("div");
                            div3.appendChild(b3);
                            div3.appendChild(document.createTextNode(article.quantity));
                            item_data.appendChild(div3);

                            item_data.appendChild(document.createElement("hr"));
                            this.element.appendChild(item_data);
                        });
                    } else {
                        this.element.innerHTML = "Your cart does not contain items from this seller";
                    }
                }
                popupTagPosition.appendChild(this.element);
            },
            close: function () {
                // get rid of the popup
                popupTagPosition.removeChild(this.element);
                // any other code you want
            }
        };
        let hoverOverMe = document.getElementById(hoverableElementId);
        hoverOverMe.onmouseover = popup.open;
        hoverOverMe.onmouseout = popup.close;
    }

})();
