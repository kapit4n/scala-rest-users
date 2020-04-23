package controllers

import javax.inject._
import play.api.{Configuration, Logger}
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future, Await}

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import dal._
import models.User

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's User page and basic informations
 */
@Singleton
class UserController @Inject()(cc: ControllerComponents, config: Configuration, repo: UserRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)

  /**
    * Return users list
    *
    * @return model.User
    */
  def list() = Action.async { implicit request =>
    repo.list().map{ data => 
      Ok(Json.toJson(data))
    }
  }

  /**
    * Return roles list
    *
    * @return model.Roles
    */
  def listRoles() = Action.async { implicit request =>
    repo.listRoles().map{ data => 
      Ok(Json.toJson(data))
    }
  }

  /**
    * Return user by id
    *
    * @return model.User
    */
  def get(id: Int) = Action.async { implicit request: Request[AnyContent] =>
    repo.get(id.toLong).map{ data => 
      Ok(Json.toJson(data(0)))
    }
  }
  

  /**
    * Return created user
    *
    * @return model.User
    */
  def create() = Action.async(parse.json) { implicit request =>
     request.body.validate[UserForm].fold({ _ => 
      Future(Ok(Json.obj("status" -> "400", "message" -> "Error parse data")))
    }, { user =>
      repo.create(user.firstName, user.lastName, user.email, user.dateOfBirth, user.gender, user.roleId).map { u => 
        Ok(Json.toJson(u))
      }
    })
  }

  /**
    * Return created role
    *
    * @return model.Role
    */
  def createRole() = Action.async(parse.json) { implicit request =>
     request.body.validate[RoleForm].fold({ _ => 
      Future(Ok(Json.obj("status" -> "400", "message" -> "Error parse data")))
    }, { role =>
      repo.createRole(role.name).map { r => 
        Ok(Json.toJson(r))
      }
    })
  }

  /**
    * Return updated user
    *
    * @return model.User
    */
  def update(id: Int) = Action.async(parse.json) { implicit request =>
     request.body.validate[UserForm].fold({ _ => 
      Future(Ok(Json.obj("status" -> "400", "message" -> "Error parse data")))
    }, { user =>
      repo.update(id, user.firstName, user.lastName, user.email, user.dateOfBirth, user.gender).map { u => 
        Ok(Json.toJson(u))
      }
    })
  }

  val userForm: Form[UserForm] = Form (
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "dateOfBirth" -> nonEmptyText,
      "gender" -> nonEmptyText,
      "roleId" -> longNumber
    )(UserForm.apply)(UserForm.unapply(_))
  )
  
  val roleForm: Form[RoleForm] = Form (
    mapping(
      "name" -> nonEmptyText,
    )(RoleForm.apply)(RoleForm.unapply(_))
  )

}

case class UserForm(firstName: String, lastName: String, email: String, dateOfBirth: String, gender: String, roleId: Long)

object UserForm {
  implicit val formatter: OFormat[UserForm] = Json.format[UserForm]
}

case class RoleForm(name: String)

object RoleForm {
  implicit val formatter: OFormat[RoleForm] = Json.format[RoleForm]
}
