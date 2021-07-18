package it.polimi.tiw.model;

import it.polimi.tiw.bean.UserBean;

public class UserModel {

    private String id;
    private String email;
    private String name;

    public UserModel(UserBean userBean) {
        this.id = userBean.getId();
        this.email = userBean.getEmail();
        this.name = userBean.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
