package controllers;

import models.User;
import play.*;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    Form<User> userForm = Form.form(User.class);


    public Result index() {
        return ok(index.render());
    }

    public Result findRoute() {
        return ok(index.render());
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
