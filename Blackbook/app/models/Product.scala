package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

import scala.language.postfixOps

case class Product(id: Long, name: String)

object Product {

  /* Parses a product from a SQL result set */
  val product = {
    get[Long]("Products.Id") ~
      get[String]("Products.Name") map {
        case id ~ name => Product(id, name)
      }
  }

  def all(): List[Product] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM Products").as(product *)
  }

  def create(name: String) {
    DB.withConnection { implicit c =>
      SQL("INSERT INTO Products(Name) VALUES ({name})").on(
        'name -> name).executeUpdate()
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit c =>
      SQL("DELETE FROM PRODUCTS WHERE ID = {id}").on(
        'id -> id).executeUpdate()
    }
  }

  def find(id: Long): Option[Product] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM Products WHERE Id = {id}").on(
      'id -> id).as(product *)
    match { 
      case found :: others => Some(found)
      case _ => None
    }
  }
  
  def find(name: String): Option[Product] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM Products WHERE Name = {name}").on(
      'name -> name).as(product *)
    match { 
      case found :: others => Some(found)
      case _ => None
    }
  }
  
  def getTags(productId: Long): List[Tag] = DB.withConnection { implicit c =>
    SQL("""
      SELECT Tags.Id, Tags.Name FROM ProductTags
        JOIN Products ON Products.Id = ProductTags.ProductId
        JOIN Tags ON Tags.Id = ProductTags.TagId
        WHERE Products.Id = {productId}
      """
      ).on('productId -> productId).as(Tag.tag *)
  }

  def addTag[A](productId: Long, tagName: String) = { 
    DB.withConnection { implicit c =>
      val tag = Tag.findOrCreate(tagName)
      SQL("""
        MERGE INTO ProductTags(ProductId, TagId) 
        VALUES ({productId}, {tagId})
      """).on(
        'productId -> productId,
        'tagId -> tag.id).executeUpdate()
    }
  }
}
