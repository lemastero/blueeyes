package blueeyes.persistence.mongo

import MongoQueryBuilder._
import MongoImplicits._
import blueeyes.json.JPathImplicits._
import blueeyes.json.JsonAST._
import MongoFilterImplicits._
import blueeyes.config.{ConfiggyModule, FilesystemConfiggyModule}
import com.google.inject.Guice
import blueeyes.json.{JsonParser, JPath, Printer}

object MongoDemo{
  private val jObject  = JObject(JField("address", JObject( JField("city", JString("A")) :: JField("street", JString("1")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject1 = JObject(JField("address", JObject( JField("city", JString("B")) :: JField("street", JString("2")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject2 = JObject(JField("address", JObject( JField("city", JString("C")) :: JField("street", JString("3")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject3 = JObject(JField("address", JObject( JField("city", JString("E")) :: JField("street", JString("4")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject6 = JObject(JField("address", JString("ll")) :: Nil)

  private val jobjectsWithArray = JsonParser.parse("""{ "foo" : [
      {
        "shape" : "square",
        "color" : "purple",
        "thick" : false
      },
      {
        "shape" : "circle",
        "color" : "red",
        "thick" : true
      }
] } """).asInstanceOf[JObject] :: Nil


  private val collection = "my-collection"

  lazy val injector = Guice.createInjector(new FilesystemConfiggyModule(ConfiggyModule.FileLoc), new RealMongoModule)

  val realMongo = injector.getInstance(classOf[Mongo])

  val database  = realMongo.database( "mydb" );
  
  def main(args: Array[String]){

//    database(remove.from(collection))

//    database[JNothing.type](ensureUniqueIndex("index").on(collection, "address.city", "address.street"))
//    demoSelectOne

    demoSelect
    
//    demoUpdate0

//    demoRemove

//    demoDistinct

//    demoGroup
  }

  private def demoSelect{
    println("------------demoSelect------------------")
    database[JNothing.type](insert(jObject, jObject1, jObject2, jObject3).into(collection))

    printObjects(database(select().from(collection).where(("address.city" === "B") || ( ("address.street" === "2") || ("address.code" === 1) ))))
//    printObjects(database(select().from(collection).sortBy("address.street" >>)))
//    printObjects(database(select().from(collection).sortBy("address.city" >>).skip(1).limit(1)))
//    printObjects(database(select().from(collection).where("address.city" === "B").sortBy("address.city" >>)))
//    printObjects(database(select().from(collection).where("address.city" === "Z").sortBy("address.city" >>)))
//    printObjects(database(select("address.city").from(collection).sortBy("address.city" >>)))
//    println(database(count.from(collection)))
//    printObjects(database(select("address.city").from(collection).sortBy("address.city" <<)))

    database(remove.from(collection))
    println("------------demoSelect------------------")
  }
  private def demoDistinct{
    println("------------demoDistinct------------------")
    database[JNothing.type](insert(jObject, jObject1, jObject2, jObject3).into(collection))
    printObjects(database(select().from(collection)))
//    val result = database(distinct("address.city").from(collection))

    database(remove.from(collection))
    println("------------demoDistinct------------------")
  }
  private def demoGroup{
    println("------------demoGroup------------------")
    val objects = JsonParser.parse("""{"address":{ "city":"A", "code":2, "street":"1"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "city":"A", "code":5, "street":"3"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "city":"C", "street":"1"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "code":3, "street":"1"  } }""").asInstanceOf[JObject] :: Nil
    database[JNothing.type](insert(objects :_*).into(collection))

    val initial = JsonParser.parse("""{ "csum": 10.0 }""").asInstanceOf[JObject]
    val result  = database(group(initial, "function(obj,prev) { prev.csum += obj.address.code; }", "address.city").from(collection))
    
    printObject(Some(result))

    database(remove.from(collection))
    println("------------demoGroup------------------")
  }
  private def demoUpdate0{
    database[JNothing.type](insert(jobjectsWithArray: _*).into(collection))

    printObjects(database(select().from(collection)))

    database(updateMany(collection).set("foo" pull (("shape" === "square") && ("color" === "purple")).elemMatch("")))

    printObjects(database(select().from(collection)))

    database(remove.from(collection))
  }
  private def demoUpdate{
    import MongoUpdateBuilder._
    println("------------demoUpdate------------------")
    insertObjects

    println(database(updateMany(collection).set("address" popFirst)))
    printObjects(database(select().from(collection)))
    println(database(update(collection).set(("address.city" unset) & ("address.street" set ("Another Street"))).where("address.city" === "C")))
    printObjects(database(select().from(collection)))
    println(database(update(collection).set(jObject3).where("address.city" === "A")))
    printObjects(database(select().from(collection)))
    println(database(updateMany(collection).set("address.street" set ("New Street"))))
    printObjects(database(select().from(collection)))

    database(remove.from(collection))
    println("------------demoUpdate------------------")
  }
  private def demoSelectOne{
    println("------------demoSelectOne------------------")
    insertObjects

    printObject(database(selectOne().from(collection).sortBy("address.city" <<)))
    printObject(database(selectOne().from(collection).sortBy("address.city" >>)))
    printObject(database(selectOne().from(collection).where("address.city" === "B").sortBy("address.city" >>)))
    printObject(database(selectOne("address.city").from(collection).sortBy("address.city" >>)))
    printObject(database(selectOne("address.city").from(collection).sortBy("address.city" <<)))

    database(remove.from(collection))
    println("------------demoSelectOne------------------")
  }

  private def printObjects(objects: Stream[JObject]){
    println("------------------------------------------------")
    println(objects.map(v => Printer.pretty(render(v))).mkString("\n"))
    println("------------------------------------------------")
  }

  private def printObject(objects: Option[JValue]){
    println("------------------------------------------------")
    println(objects.map(v => Printer.pretty(render(v))).mkString("\n"))
    println("------------------------------------------------")
  }

  private def demoRemove{
    println("------------demoRemove------------------")

    insertObjects

    println("REMOVED=" + database(remove.from(collection).where("address.city" === "A")))

    println("REMOVED=" + database(remove.from(collection)))

    println("------------demoRemove------------------")
  }

  private def insertObjects{
    database[JNothing.type](insert(jObject, jObject).into(collection))
  }
}

//db.foo.insert({"address":{ "city":"A", "code":2, "street":"1"  } })
//db.foo.insert({"address":{ "city":"C", "street":"1"  } })
//db.foo.insert({"address":{ "code":33, "street":"5"  } })
//
//db.foo.group({key: { "address.city":1 }, cond: {}, reduce: function(a,b) { b.csum += a.address.code; return b;}, initial: { csum: 0 } })


//{ group: { key: { address.city: 1.0 }, cond: {}, initial: { csum: 12.0 }, $reduce: function (obj, prev) {prev.csum += obj.address.code;} } }
//{ group: { key: { address.city: 1.0 }, cond: {}, initial: { csum: 10 }, $reduce: "function(obj,prev) { prev.csum += obj.address.code;}" } }