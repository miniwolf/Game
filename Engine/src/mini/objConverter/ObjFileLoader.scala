package mini.objConverter

import mini.math.{Vector2f, Vector3f}
import mini.utils.MyFile

import collection.JavaConverters._

/**
  * Created by miniwolf on 24-03-2017.
  */
class Face(vertices: Array[Vertex])

object ObjFileLoader {
  var matFaces: Map[String, List[Face]]
  var currentMatName: String

  def getVector3(s: String): Vector3f = {
    s.trim.split(" ") match {
      case Array(x: String, y: String, z: String) =>
        new Vector3f(x.toFloat, y.toFloat, z.toFloat);
    }
  }

  def getVector2(s: String): Vector2f = {
    s.trim.split(" ") match {
      case Array(x: String, y: String) =>
        new Vector2f(x.toFloat, y.toFloat);
    }
  }

  private def processMaterialLib(line: String, path: String): Map[String, List[Face]] = {
    line.substring("mtllib".length).trim.split(" ").flatMap {
      libname =>
        val matList = MTLFileLoader.load(new MyFile(path, libname))
        matList.keys.map(matName => matName -> List[Face]())
    }.toMap
  }

  def loadOBJ(objFile: MyFile): ModelData = {
    def parseLines(lines: List[String], vertices: List[Vertex], texCoords: List[Vector2f],
                   normals: List[Vector3f], indices: List[Int])
    : (List[Vertex], List[Vector2f], List[Vector3f], List[Int]) = {
      lines match {
        case line :: cdr =>
          line match {
            case x if x startsWith "v " =>
              val v = getVector3(x.substring(2))
              val newVertices = new Vertex(vertices.size, v) :: vertices
              return parseLines(cdr, newVertices, texCoords, normals, indices)
            case x if x startsWith "vt " =>
              val vt = getVector2(x.substring(3))
              return parseLines(cdr, vertices, vt :: texCoords, normals, indices)
            case x if x startsWith "vn " =>
              val vn = getVector3(x.substring(3))
              return parseLines(cdr, vertices, texCoords, vn :: normals, indices)
            case x if x startsWith "mtllib" =>
              matFaces = processMaterialLib(line, objFile.getDirectory)
            case x if x startsWith "usemtl" =>
              currentMatName = x.substring("usermtl".length).trim
          }
          parseLines(cdr, vertices, texCoords, normals, indices)
        case _ => (vertices, texCoords, normals, indices)
      }
    }

    val (verts, texs, norms, inds) =
      parseLines(objFile.getLines.asScala.toList, List(), List(), List(), List())
    new ModelData(verts.toArray, texs.toArray, norms.toArray, inds.toArray)
  }
}
