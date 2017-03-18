package mini.objConverter

import java.util

import mini.material.Material
import mini.math.ColorRGBA
import mini.utils.MyFile

import collection.JavaConverters._

/**
  * Created by miniwolf on 17-03-2017.
  */
object MTLFileLoader {
  def load(mtlFile: MyFile): util.Map[String, Material] = {
    def parseLines(lines: List[String], material: Material, matList: Map[String, Material])
    : Map[String, Material] = {
      lines match {
        case line :: cdr =>
          line match {
            case x: String if x startsWith "newmtl" =>
              val matName = x.substring("newmtl".length).trim
              val newMaterial = new Material(matName)
              val newMatList = matList + (matName -> newMaterial)
              parseLines(cdr, newMaterial, newMatList)
            case x: String if x startsWith "Ns" =>
              material.setFloat("Shininess", x.substring("Ns".length).trim.toFloat)
              parseLines(cdr, material, matList)
            case x: String if x startsWith "Ka" =>
              x.substring("Ka".length).trim.split(" ") match {
                case Array(r: String, g: String, b: String) =>
                  material.setColor("Ambient",
                    new ColorRGBA(r.toFloat, g.toFloat, b.toFloat, 1.0f));
              }
              parseLines(cdr, material, matList)
            case x: String if x startsWith "Kd" =>
              x.substring("Kd".length).trim.split(" ") match {
                case Array(r: String, g: String, b: String) =>
                  material.setColor("Diffuse",
                    new ColorRGBA(r.toFloat, g.toFloat, b.toFloat, 1.0f));
              }
              parseLines(cdr, material, matList)
            case x: String if x startsWith "Ks" =>
              x.substring("Ks".length).trim.split(" ") match {
                case Array(r: String, g: String, b: String) =>
                  material.setColor("Specular",
                    new ColorRGBA(r.toFloat, g.toFloat, b.toFloat, 1.0f));
              }
              parseLines(cdr, material, matList)
            case x: String if x startsWith "#" =>
              parseLines(cdr, material, matList)
            case s =>
              println(s"Unsupported value $s")
              parseLines(cdr, material, matList)

          }
        case _ => matList
      }
    }

    parseLines(mtlFile.getLines.asScala.toList, null, Map[String, Material]()).asJava
  }
}
