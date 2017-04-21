package mini.objConverter

import mini.material.Material
import mini.math.ColorRGBA
import mini.utils.MyFile
import mini.shaders.VarType
import mini.textures.{Texture, TextureBuilder}

import collection.JavaConverters._

/**
  * Created by miniwolf on 17-03-2017.
  */
object MTLFileLoader {
  def load(mtlFile: MyFile): Map[String, Material] = {
    def parseLines(lines: List[String], material: Material, matList: Map[String, Material])
    : Map[String, Material] = {
      lines match {
        case line :: cdr =>
          line match {
            case x: String if x startsWith "newmtl" =>
              val matName = x.substring("newmtl".length).trim
              val newMaterial = new Material(matName)
              val newMatList = matList + (matName -> newMaterial)
              return parseLines(cdr, newMaterial, newMatList)
            case x: String if x startsWith "Ns" =>
              material.setFloat("Shininess", x.substring("Ns".length).trim.toFloat)
            case x: String if x startsWith "Ka" =>
              material.setColor("Ambient", createColorParam(x))
            case x: String if x startsWith "Kd" =>
              material.setColor("Diffuse", createColorParam(x))
            case x: String if x startsWith "Ks" =>
              material.setColor("Specular", createColorParam(x))
            case x: String if x.isEmpty =>
            case x: String if x startsWith "#" =>
            case s =>
              println(s"Unsupported value $s")
          }
          parseLines(cdr, material, matList)
        case _ => matList
      }
    }

    parseLines(mtlFile.getLines.asScala.toList, null, Map[String, Material]())
  }

  private def createColorParam(x: String): ColorRGBA = {
    x.substring(2).trim.split(" ") match {
      case Array(r: String, g: String, b: String) =>
        new ColorRGBA(r.toFloat, g.toFloat, b.toFloat, 1.0f)
    }
  }
}
