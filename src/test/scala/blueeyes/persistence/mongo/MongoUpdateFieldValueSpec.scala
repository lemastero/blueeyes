package blueeyes.persistence.mongo

import org.spex.Specification
import blueeyes.json.JPathImplicits._
import MongoUpdateOperators._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonParser

class MongoUpdateFieldValueSpec  extends Specification{
  "build valid json" in {
    import MongoFilterImplicits._
    MongoUpdateFieldValue($inc, "n", "" === 1).toJValue mustEqual  (JObject(JField("$inc", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "build valid json for neset MongoQuery" in {
    import MongoFilterImplicits._
    MongoUpdateFieldValue($pull, "foo", "bar" === 1).toJValue mustEqual  (JObject(JField("$pull", JObject(JField("foo", JObject(JField("bar", JInt(1)) :: Nil)) :: Nil)) :: Nil))
  }
  "build valid json for pull and for elemMatch" in {
    import MongoFilterImplicits._
    MongoUpdateFieldValue($pull, "foo", MongoAndFilter(("bar" === 1) :: Nil).elemMatch("")).toJValue mustEqual  (JsonParser.parse(""" {"$pull": {"foo": {"$elemMatch" : {"bar": 1} }}} """))
  }
}