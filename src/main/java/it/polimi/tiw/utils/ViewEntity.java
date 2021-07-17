
package it.polimi.tiw.utils;

public class ViewEntity {

    private String userId;
    private String articleId;

    public ViewEntity(String userId, String articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getArticleId() {

        return articleId;
    }

    public void setArticleId(String articleId) {

        this.articleId = articleId;
    }

    @Override
    public String toString() {

        return "ViewEntity{" + "userId='" + userId + '\'' + ", articleId='" + articleId + '\'' + '}';
    }
}
