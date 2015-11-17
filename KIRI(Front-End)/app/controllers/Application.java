package controllers;

import models.User;
import play.*;
import play.data.Form;
import play.db.DB;
import play.mvc.*;

import views.html.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Application extends Controller {

    Form<User> userForm = Form.form(User.class);


    public Result index() {
        ctx().changeLang("en");
        return ok(index.render());
    }

    //i18n
    public Result id() {
        ctx().changeLang("id");
        return ok(index.render());
    }

    public Result findRoute() {
        return ok(index.render());
    }

    public Result testdb() {
        Connection connection = null;
        StringBuilder output = new StringBuilder();
        try {
            connection = DB.getConnection();
            // Look for angkot.web.id refreshes
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SHOW TABLES;");
            while (result.next()) {
                output.append(result.getString(1) + "/");
            }
            connection.close();
            return ok(output.toString());
        } catch (Exception e) {
            return internalServerError(e.toString());
        } finally {
            if (connection != null) {
//                      connection.close();
            }
        }
    }

//    public Result submit()
//    {
//        Form<User> boundForm = userForm.bindFromRequest();
//
//        if(boundForm.hasErrors())
//        {
//            flash("error","Please correct the form");
//            return badRequest(index.render(boundForm));
//        }
//
//        User user = boundForm.get();
//        flash("Success","Calculating routes");
//        return redirect("/");
//
//    }

}
