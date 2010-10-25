package blueeyes.core.http

import scala.util.matching.Regex
import blueeyes.util.ProductPrefixUnmangler

/*
Usage: 

import MimeTypes._
val mimeType = image/gif
val imageMimeType = image/gif 
val javaScriptMimeType = text/javascript
*/

sealed trait MimeType {
  def maintype: String
  def subtype: String
  
  def value: String = maintype + "/" + subtype
  
  def extensions: List[String] = subtype :: Nil
  
  def defaultExtension = extensions.head
}

object MimeTypes {

  def parseMimeTypes(inString: String): Array[MimeType] = {
    def MimeTypeRegex = new Regex("""([a-z\-]+)/([.+a-z\-]+)""")

    /* Split the string on commas, which separate the mimes */
    var outMimes: Array[MimeType] = inString.toLowerCase.split(",").map(_.trim)
      .flatMap(MimeTypeRegex findFirstIn _).map(_.split("/"))
      .flatMap ( mimeType =>  mimeType match {

        case Array("*" , "*")                     => Array(anymaintype / anysubtype)

        case Array("application", "javascript")   => Array(application / javascript)
        case Array("application", "x-javascript") => Array(application / `x-javascript`)
        case Array("application", "soap+xml")     => Array(application / `soap+xml`) 
        case Array("application", "xhtml+xml")    => Array(application / `xhtml+xml`)
        case Array("application", "xml-dtd")      => Array(application / `xml-dtd`)
        case Array("application", "json")         => Array(application / json)
        case Array("application", "x-latex")      => Array(application / `x-latex`) 
        case Array("application", "octet-stream") => Array(application / `octet-stream`)
        case Array("application", "ogg" )         => Array(application / ogg)
        case Array("application", "pdf" )         => Array(application / `pdf`)
        case Array("application", "postscript" )  => Array(application / `postscript`)
        case Array("application", "x-dvi")        => Array(application / `x-dvi`)
        case Array("application", "x-shockwave-flash") => Array(application / `x-shockwave-flash`)
        case Array("application", "x-tar")        => Array(application / `x-tar`)
        case Array("application", "x-ttf")        => Array(application / `x-ttf`)
        case Array("application", "zip")          => Array(application / `zip`)

        /* Audio */
        case Array("audio", "basic")              => Array(audio / basic)
        case Array("audio", "mp4")                => Array(audio / mp4)
        case Array("audio", "midi")               => Array(audio / midi)
        case Array("audio", "mpeg")               => Array(audio / mpeg)
        case Array("audio", "vorbis")             => Array(audio / vorbis)
        case Array("audio", "x-ms-wma")           => Array(audio / `x-ms-wma`)
        case Array("audio", "x-ms-wax")           => Array(audio / `x-ms-wax`)
        case Array("audio", "x-realaudio")        => Array(audio / `x-realaudio`)
        case Array("audio", "x-wav")              => Array(audio / `x-wav`)

        /* Image */
        case Array("image", "gif")                => Array(image / gif)
        case Array("image", "png")                => Array(image / png)
        case Array("image", "jpeg")               => Array(image / jpeg)
        case Array("image", "svg+xml")            => Array(image / `svg+xml`)
        case Array("image", "tiff")               => Array(image / tiff)
        case Array("image", "vnd.microsoft.icon") => Array(image / `vnd.microsoft.icon`)

        /* Text */
        case Array("text", "css")                 => Array(text / css)
        case Array("text", "csv")                 => Array(text / csv)
        case Array("text", "html")                => Array(text / html)
        case Array("text", "plain")               => Array(text / plain)
        case Array("text", "x-c")                 => Array(text / `x-c`)
        case Array("text", "xml")                 => Array(text / xml)

        /* Video */
        case Array("video", "quicktime")          => Array(video / quicktime)
        case Array("video", "x-msvideo")          => Array(video / `x-msvideo`)

        /* Star */

        case _ => Nil
      }
    )
    return outMimes 
  }

  trait GenericType extends ProductPrefixUnmangler {
    def subtype = unmangledName
  }

  sealed abstract class StarType(val extensions: List[String]) extends GenericType
  sealed abstract class ApplicationType(val extensions: List[String]) extends GenericType
  sealed abstract class AudioType(val extensions: List[String]) extends GenericType
  sealed abstract class ImageType(val extensions: List[String]) extends GenericType
  sealed abstract class MessageType(val extensions: List[String]) extends GenericType
  sealed abstract class MultipartType(val extensions: List[String]) extends GenericType
  sealed abstract class PrsType(val extensions: List[String]) extends GenericType
  sealed abstract class TextType(val extensions: List[String]) extends GenericType
  sealed abstract class VideoType(val extensions: List[String]) extends GenericType

  /* Star Type */
  case object anysubtype extends StarType(Nil) {
    override def subtype = "*"
  }

  /* Application Types */
  sealed abstract class JavaScriptApplicationType extends ApplicationType("js" :: Nil)
  sealed abstract class OggApplicationType extends ApplicationType("ogg" :: Nil)

  case object javascript extends JavaScriptApplicationType
  case object `x-javascript` extends JavaScriptApplicationType
  case object `soap+xml` extends ApplicationType("soap+xml" :: Nil)
  case object `xhtml+xml` extends ApplicationType("xhtml+xml" :: Nil)
  case object `xml-dtd` extends ApplicationType("xml-dtd" :: Nil)

  case object json extends ApplicationType("json" :: Nil)
  case object `x-latex` extends ApplicationType("latex" :: Nil)
  case object `octet-stream` extends ApplicationType("bin" :: "class" :: "dms" :: "exe" :: "lha" :: "lzh" :: Nil)
  case object ogg extends JavaScriptApplicationType
  case object pdf extends ApplicationType("pdf" :: Nil)
  case object postscript extends ApplicationType("ai" :: Nil)
  case object `x-dvi` extends ApplicationType("dvi" :: Nil)
  case object `x-shockwave-flash` extends ApplicationType("swf" :: Nil)
  case object `x-tar` extends ApplicationType("tar" :: Nil)
  case object `x-ttf` extends ApplicationType("ttf" :: Nil)
  case object zip extends ApplicationType("zip" :: Nil)

  /* Audio Types */
  sealed abstract class MpegAudioType extends AudioType("mpg" :: "mpeg" :: "mpga" :: "mpe" :: "mp3" :: "mp2" :: Nil)
  sealed abstract class Mp4AudioType extends AudioType("mp4" :: Nil)

  case object basic extends AudioType("au" :: "snd" :: Nil)
  case object mp4 extends Mp4AudioType
  case object midi extends AudioType("midi" :: "mid" :: "kar" :: Nil)
  case object mpeg extends MpegAudioType
  case object vorbis extends AudioType("vorbis" :: Nil)
  case object `x-ms-wma` extends AudioType("wma" :: Nil)
  case object `x-ms-wax` extends AudioType("wax" :: Nil)
  case object `x-realaudio` extends AudioType("ra" :: Nil)
  case object `x-wav` extends AudioType("wav" :: Nil)

  /* Image Types */ 
  case object gif extends ImageType("gif" :: Nil)
  case object png extends ImageType("png" :: Nil)
  case object jpeg extends ImageType("jpg" :: "jpeg" :: "jpe" :: Nil)
  case object `svg+xml` extends ImageType("svg+xml" :: Nil)
  case object tiff extends ImageType("tiff" :: Nil)
  case object `vnd.microsoft.icon` extends ImageType("ico" :: Nil)

  /* Message Types */
  case object http extends MessageType("http" :: Nil)
  case object `delivery-status` extends MessageType("delivery-status" :: Nil)

  /* Multipart Types */
  case object mixed extends MultipartType("mixed" :: Nil)
  case object alternative extends MultipartType("alternative" :: Nil)
  case object related extends MultipartType("related" :: Nil)
  case object `form-data` extends MultipartType("form-data" :: Nil)
  case object signed extends MultipartType("signed" :: Nil)
  case object encrypted extends MultipartType("encrypted" :: Nil)

  /* Text Types */
  case object css extends TextType("css" :: Nil)
  case object csv extends TextType("csv" :: Nil)
  case object html extends TextType("html" :: "htm" :: Nil)
  case object plain extends TextType("c" :: "c++" :: "cc" :: "com" :: "conf" :: "f" :: "h" :: "jav" :: "pl" :: "text" :: "txt" :: Nil)
  case object `x-c` extends TextType("c" :: Nil)
  case object xml extends TextType("xml" :: Nil)

  /* Video Types */
  case object quicktime extends VideoType("qt" :: "mov" :: Nil)
  case object `x-msvideo` extends VideoType("avi" :: Nil)

  /* Implicit Conversions */
  implicit def applicationTypeJavaScript2TextTypeJavaScript(appType: JavaScriptApplicationType): TextType = {
    case object TextTypeJavaScript extends TextType(appType.extensions) { 
      override def subtype = appType.subtype 
    }
    return TextTypeJavaScript
  }

  implicit def applicationTypeOgg2AudioTypeOgg(appType: OggApplicationType ): AudioType = {
    case object AudioTypeOgg extends AudioType(appType.extensions) {
      override def subtype = appType.subtype
    }
    return AudioTypeOgg
  }

  implicit def applicationTypeOgg2VideoTypeOgg(appType: OggApplicationType): VideoType = {
    case object VideoTypeOgg extends VideoType(appType.extensions) {
      override def subtype = appType.subtype 
    }
    return VideoTypeOgg
  }

  implicit def audioTypeMpeg2VideoTypeMpeg (audioType: MpegAudioType): VideoType = {
    case object VideoTypeMpeg extends VideoType(audioType.extensions) {
      override def subtype = audioType.subtype
    }
    return VideoTypeMpeg
  }

  implicit def audioTypeMp42VideoTypeMp4 (audioType: Mp4AudioType): VideoType = {
    case object VideoTypeMp4 extends VideoType(audioType.extensions) {
      override def subtype = audioType.subtype
    }
    return VideoTypeMp4
  }

  /* Implicit conversions for Star (*) */

  implicit def starTypeStar2ApplicationTypeStar (starType: StarType): ApplicationType = {
    case object ApplicationTypeStar extends ApplicationType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return ApplicationTypeStar 
  }

  implicit def starTypeStar2AudioTypeStar (starType: StarType): AudioType = {
    case object AudioTypeStar extends AudioType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return AudioTypeStar 
  }

  implicit def starTypeStar2ImageTypeStar (starType: StarType): ImageType = {
    case object ImageTypeStar extends ImageType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return ImageTypeStar 
  }

  implicit def starTypeStar2MessageTypeStar (starType: StarType): MessageType = {
    case object MessageTypeStar extends MessageType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return MessageTypeStar 
  }

  implicit def starTypeStar2MultipartTypeStar (starType: StarType): MultipartType = {
    case object MultipartTypeStar extends MultipartType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return MultipartTypeStar 
  }

  implicit def starTypeStar2PrsTypeStar (starType: StarType): PrsType = {
    case object PrsTypeStar extends PrsType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return PrsTypeStar 
  }

  implicit def starTypeStar2TextTypeStar (starType: StarType): TextType = {
    case object TextTypeStar extends TextType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return TextTypeStar 
  }

  implicit def starTypeStar2VideoTypeStar (starType: StarType): VideoType = {
    case object VideoTypeStar extends VideoType(starType.extensions) {
      override def subtype = starType.subtype
    }
    return VideoTypeStar 
  }


  /* Constructor Methods */
  object anymaintype {
    def / (starType: StarType) = new MimeType {
      def maintype = "*"
      def subtype = starType.subtype
      override def extensions = starType.extensions
    }
  }

  object application {
    def / (applicationType: ApplicationType) = new MimeType {
      def maintype = "application"
      def subtype = applicationType.subtype 
      override def extensions = applicationType.extensions
    }
  }

  object audio {
    def / (audioType: AudioType) = new MimeType {
      def maintype = "audio"
      def subtype = audioType.subtype 
      override def extensions = audioType.extensions
    }
  }

  object image {
    def / (imageType: ImageType) = new MimeType {
      def maintype = "image"
      def subtype = imageType.subtype 
      override def extensions = imageType.extensions
    }
  }

  object message {
    def / (messageType: MessageType) = new MimeType {
      def maintype = "message"
      def subtype = messageType.subtype 
      override def extensions = messageType.extensions
    }
  }

  object multipart {
    def / (multipartType: MultipartType) = new MimeType {
      def maintype = "multipart"
      def subtype = multipartType.subtype 
      override def extensions = multipartType.extensions
    }
  }

  object text {
    def / (textType: TextType) = new MimeType {
      def maintype = "text"
      def subtype = textType.subtype 
      override def extensions = textType.extensions
    }
  }

  object video {
    def / (videoType: VideoType) = new MimeType {
      def maintype = "text"
      def subtype = videoType.subtype
      override def extensions = videoType.extensions
    }
  }
}

