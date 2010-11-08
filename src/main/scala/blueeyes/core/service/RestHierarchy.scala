package blueeyes.core.service

import blueeyes.util.Future
import blueeyes.core.http._
import blueeyes.core.http.HttpMethods._
import blueeyes.core.http.HttpHeaders._
import blueeyes.core.http.HttpHeaderImplicits._
import net.lag.configgy.{Config, ConfigMap}
import net.lag.logging.Logger

trait RestHierarchy[S] {
  private type Parameters[T] = (RestPathPattern, HttpMethod, HttpRequest[T] => Future[HttpResponse[T]], HttpDataTranscoder[T, S])
  def hierarchy: List[Parameters[_]]
}

trait RestHierarchyBuilder[S] extends RestHierarchy[S] {
  import scala.collection.mutable.{Stack, ArrayBuffer}
  
  private type Handler[T] = HttpRequest[T] => Future[HttpResponse[T]]
  private type Parameters[T] = (RestPathPattern, HttpMethod, Handler[T], HttpDataTranscoder[T, S])
  
  private val pathStack: Stack[RestPathPattern] = new Stack[RestPathPattern].push(RestPathPattern.Root);
  private val _hierarchy: ArrayBuffer[Parameters[_]] = new ArrayBuffer
  
  def hierarchy = _hierarchy.toList
  
  def build = hierarchy
  
  def path(path: RestPathPattern)(f: => Unit): Unit = {
    pathStack.push(path)
    
    try { f } finally { pathStack.pop() }
  }
  
  def get[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(GET, handler, t)
  
  def put[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(PUT, handler, t)
  
  def post[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(POST, handler, t)
  
  def delete[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(DELETE, handler, t)
  
  def options[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(OPTIONS, handler, t)
  
  def head[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(HEAD, handler, t)
  
  def connect[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(CONNECT, handler, t)
  
  def trace[T](handler: Handler[T])(implicit t: HttpDataTranscoder[T, S]) = custom(TRACE, handler, t)
  
  def custom[T](method: HttpMethod, handler: Handler[T], t: HttpDataTranscoder[T, S]) = {
    _hierarchy += ((currentPath, method, handler, t))
  }
  
  private def currentPath: RestPathPattern = pathStack.foldRight[RestPathPattern](RestPathPattern.Root) { (element, path) => path + element }
  
  //private def currentPath: RestPathPattern = { println(pathStack); pathStack.foldLeft[RestPathPattern](RestPathPattern.Root) { (path, element) => path / element } }
}

import blueeyes.core.data._
import blueeyes.core.http.MimeType
import blueeyes.core.http.MimeTypes._
import blueeyes.json.JsonAST.JValue

sealed trait HttpResponseType[T]

case object HttpResponseStringType extends HttpResponseType[String]
case object HttpResponseBytesType  extends HttpResponseType[Array[Byte]]

trait HttpDataTranscoder[T, S] extends DataTranscoder[T, S] { self =>
  def responseType: HttpResponseType[S]
  
  def inverse = new HttpDataTranscoder[S, T] {
    def transcode = self.transcode.inverse
    
    def responseType = error("bad design")
    
    def mimeType = self.mimeType
  }
}

class HttpStringDataTranscoder[T](transcode: Bijection[T, String], mimeType: MimeType) extends DataTranscoderImpl[T, String](transcode, mimeType) with HttpDataTranscoder[T, String]{
  val responseType: HttpResponseType[String] = HttpResponseStringType
}
class HttpBytesDataTranscoder[T](transcode: Bijection[T, Array[Byte]], mimeType: MimeType) extends DataTranscoderImpl[T, Array[Byte]](transcode, mimeType) with HttpDataTranscoder[T, Array[Byte]]{
  val responseType: HttpResponseType[Array[Byte]] = HttpResponseBytesType
}

object Transcoders{
  implicit val HttpJsonToText = new HttpStringDataTranscoder[JValue](JsonToTextBijection, application/json)
}
