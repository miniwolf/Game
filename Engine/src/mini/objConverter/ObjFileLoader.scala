package mini.objConverter

import java.nio.FloatBuffer

import mini.material.Material
import mini.math.{Vector2f, Vector3f}
import mini.scene._
import mini.scene.mesh.{IndexBuffer, IndexIntBuffer, IndexShortBuffer}
import mini.utils.{BufferUtils, MyFile}

import collection.JavaConverters._
import scala.annotation.tailrec

/**
  * Created by miniwolf on 24-03-2017.
  */
class Face(val vertices: List[Vertex])

object ObjFileLoader {
  private var matFaces: Map[String, List[Face]] = Map()
  private var matList: Map[String, Material] = Map()
  private var currentMatName: String = ""
  private var faces: List[Face] = List[Face]()
  private var vertIndexMap = Map[Vertex, Int]()
  private var indexVertMap = Map[Int, Vertex]()
  private var curIndex = 0
  private var geomIndex = 0
  private var objName = ""
  private var objNode: Node = _

  private def processMaterialLib(line: String, path: String): Map[String, List[Face]] = {
    line.substring("mtllib".length).trim.split(".mtl ").flatMap {
      libname =>
        matList = MTLFileLoader.load(new MyFile(path, libname))
        matList.keys.map(matName => matName -> List[Face]())
    }.toMap
  }

  @tailrec
  private def setupFaces(faceList: List[Face], newFaces: List[Face], hasTexCoord: Boolean = false,
                 hasNormals: Boolean = false): (List[Face], Boolean, Boolean) = {
    faceList match {
      case ((f: Face) :: cdr) =>
        var newHasTexCoord = hasTexCoord
        var newHasNormal = hasNormals
        f.vertices.foreach((v: Vertex) => {
          findVertexIndex(v)

          if (!newHasTexCoord && v.texCoord.isDefined) {
            newHasTexCoord = true
          }
          if (!newHasNormal && v.normal.isDefined) {
            newHasNormal = true
          }
        })
        if (f.vertices.length == 4) {
          val (t1, t2) = quadToTriangle(f)
          setupFaces(cdr, t1 :: t2 :: newFaces, newHasTexCoord, newHasNormal)
        } else {
          setupFaces(cdr, f :: newFaces, newHasTexCoord, newHasNormal)
        }
      case _ =>
        (newFaces, hasTexCoord, hasNormals)
    }
  }

  private def quadToTriangle(f: Face) = {
    assert(f.vertices.length == 4)
    val v0 = f.vertices.head
    val v1 = f.vertices(1)
    val v2 = f.vertices(2)
    val v3 = f.vertices(3)
    // find the pair of vertices that is closest to each over
    // v0 and v2
    // OR
    // v1 and v3
    val d1 = v0.vertex.distanceSquared(v2.vertex)
    val d2 = v1.vertex.distanceSquared(v3.vertex)
    if (d1 < d2) { // put an edge in v0, v2
      val t1 = new Face(List(v0, v1, v3))
      val t2 = new Face(List(v1, v2, v3))
      (t1, t2)
    } else { // put an edge in v1, v3
      val t1 = new Face(List(v0, v1, v2))
      val t2 = new Face(List(v0, v2, v3))
      (t1, t2)
    }
  }

  private def constructMesh(faceList: List[Face]) = {
    val m = new Mesh()
    m.setMode(Mesh.Mode.Triangles)
    val (newFaces, hasTexCoord, hasNormals) = setupFaces(faceList, List())

    val posBuf = BufferUtils.createFloatBuffer(vertIndexMap.size * 3)
    var normBuf: FloatBuffer = null
    var tcBuf: FloatBuffer = null

    var indexBuf: IndexBuffer = null
    if (vertIndexMap.size >= 65536) { // too many verticies: use intbuffer instead of shortbuffer
      val ib = BufferUtils.createIntBuffer(newFaces.size * 3)
      m.setBuffer(VertexBuffer.Type.Index, 3, ib)
      indexBuf = new IndexIntBuffer(ib)
    } else {
      val sb = BufferUtils.createShortBuffer(newFaces.size * 3)
      m.setBuffer(VertexBuffer.Type.Index, 3, sb)
      indexBuf = new IndexShortBuffer(sb)
    }

    if (hasNormals) {
      normBuf = BufferUtils.createFloatBuffer(vertIndexMap.size * 3)
      m.setBuffer(VertexBuffer.Type.Normal, 3, normBuf)
    }

    if (hasTexCoord) {
      tcBuf = BufferUtils.createFloatBuffer(vertIndexMap.size * 2)
      m.setBuffer(VertexBuffer.Type.TexCoord, 2, tcBuf)
    }

    val nPosBuf = setupBuffers(0, newFaces, posBuf, normBuf, tcBuf, indexBuf)
    m.setBuffer(VertexBuffer.Type.Position, 3, nPosBuf)

    m.setStatic()
    m.updateCounts()

    vertIndexMap = Map()
    indexVertMap = Map()
    curIndex = 0

    m
  }

  @tailrec
  private def setupBuffers(index: Int, faces: List[Face], posBuf: FloatBuffer,
                           normBuf: FloatBuffer, tcBuf: FloatBuffer, indexBuf: IndexBuffer)
  : FloatBuffer = {
    faces match {
      case (f: Face) :: cdr if f.vertices.length != 3 =>
        setupBuffers(index + 1, cdr, posBuf, normBuf, tcBuf, indexBuf)
      case (f: Face) :: cdr =>
        val v0 = f.vertices.head
        val v1 = f.vertices(1)
        val v2 = f.vertices(2)

        posBuf.position(v0.index * 3)
        posBuf.put(v0.vertex.x).put(v0.vertex.y).put(v0.vertex.z)
        posBuf.position(v1.index * 3)
        posBuf.put(v1.vertex.x).put(v1.vertex.y).put(v1.vertex.z)
        posBuf.position(v2.index * 3)
        posBuf.put(v2.vertex.x).put(v2.vertex.y).put(v2.vertex.z)

        if (normBuf != null && v0.normal.isDefined) {
          normBuf.position(v0.index * 3)
          val v0N = v0.normal.get
          normBuf.put(v0N.x).put(v0N.y).put(v0N.z)

          normBuf.position(v1.index * 3)
          val v1N = v1.normal.get
          normBuf.put(v1N.x).put(v1N.y).put(v1N.z)

          normBuf.position(v2.index * 3)
          val v2N = v2.normal.get
          normBuf.put(v2N.x).put(v2N.y).put(v2N.z)
        }

        if (tcBuf != null && v0.texCoord.isDefined) {
          tcBuf.position(v0.index * 2)
          val v0Tc = v0.texCoord.get
          tcBuf.put(v0Tc.x).put(v0Tc.y)
          tcBuf.position(v1.index * 2)
          val v1Tc = v1.texCoord.get
          tcBuf.put(v1Tc.x).put(v1Tc.y)
          tcBuf.position(v2.index * 2)
          val v2Tc = v2.texCoord.get
          tcBuf.put(v2Tc.x).put(v2Tc.y)
        }
        val bufIndex = index * 3 // current face * 3 = current index
        indexBuf.put(bufIndex, v0.index)
        indexBuf.put(bufIndex + 1, v1.index)
        indexBuf.put(bufIndex + 2, v2.index)
        setupBuffers(index + 1, cdr, posBuf, normBuf, tcBuf, indexBuf)
      case _ =>
        posBuf
    }
  }

  protected def findVertexIndex(vert: Vertex): Unit = {
    vertIndexMap.get(vert) match {
      case None =>
        vert.index = curIndex
        curIndex += 1
        vertIndexMap += (vert -> vert.index)
        indexVertMap += (vert.index -> vert)
      case Some(index) => vert.index = index
    }
  }

  private def createGeometry(faceList: List[Face], matName: String): Geometry = {
    // Create mesh from the faces
    val mesh = constructMesh(faceList)

    val geom = new Geometry(objName + "-geom-" + geomIndex, mesh)
    geomIndex += 1

    var material: Option[Material] = None
    if (matName != null && matList != null) { // Get material from material list
      material = Some(matList(matName))
    }
    if (material.isEmpty) { // create default material
      material = Option(new Material("Engine/MatDefs/Light/Lighting.minid"))
      material.get.setFloat("Shininess", 64)
    }
    geom.setMaterial(material.get)
    geom
  }

  def reset(): Unit = {
    faces = List()
    matFaces = Map()
    vertIndexMap = Map()
    indexVertMap = Map()
    currentMatName = null
    matList = Map()
    curIndex = 0
    geomIndex = 0
  }

  def loadOBJ(objFile: MyFile): Spatial = {
    reset()
    @tailrec
    def parseLines(lines: List[String], vertices: List[Vector3f], texCoords: List[Vector2f],
                   normals: List[Vector3f], indices: List[Int])
    : (List[Vector3f], List[Vector2f], List[Vector3f], List[Int]) = {
      lines match {
        case line :: cdr =>
          line match {
            case x if x startsWith "v " =>
              val v = getVector3(x.substring(2))
              parseLines(cdr, v :: vertices, texCoords, normals, indices)
            case x if x startsWith "vt " =>
              val vt = getVector2(x.substring(3))
              parseLines(cdr, vertices, vt :: texCoords, normals, indices)
            case x if x startsWith "vn " =>
              val vn = getVector3(x.substring(3))
              parseLines(cdr, vertices, texCoords, vn :: normals, indices)
            case x if x startsWith "mtllib" =>
              matFaces = processMaterialLib(line, objFile.getDirectory)
              parseLines(cdr, vertices, texCoords, normals, indices)
            case x if x startsWith "usemtl" =>
              currentMatName = x.substring("usermtl".length).trim
              parseLines(cdr, vertices, texCoords, normals, indices)
            case x if x startsWith "f " =>
              readFace(x.substring(2).trim, vertices, texCoords, normals)
              parseLines(cdr, vertices, texCoords, normals, indices)
            case x =>
              println(s"Unsupported character: $x")
              parseLines(cdr, vertices, texCoords, normals, indices)
          }
        case _ => (vertices, texCoords, normals, indices)
      }
    }

    val objNode = new Node(objFile.getDirectory + "-objNode")

    val (verts, texs, norms, inds) =
      parseLines(objFile.getLines.asScala.toList, List(), List(), List(), List())
    if (matFaces.nonEmpty) {
      matFaces.foreach {
        case (matName, faceList) =>
          if (faceList.nonEmpty) {
            val geom = createGeometry(faceList, matName)
            objNode.attachChild(geom)
          }
      }
    } else {
      val geom: Geometry = createGeometry(faces, null)
      objNode.attachChild(geom)
    }
    if (objNode.getQuantity == 1) {
      // only 1 geometry, so no need to send node
      objNode.getChild(0)
    } else {
      objNode
    }
  }

  private def getVector3(s: String): Vector3f = {
    s.trim.split(" ") match {
      case Array(x: String, y: String, z: String) =>
        new Vector3f(x.toFloat, y.toFloat, z.toFloat);
    }
  }

  private def getVector2(s: String): Vector2f = {
    s.trim.split(" ") match {
      case Array(x: String, y: String) =>
        new Vector2f(x.toFloat, y.toFloat);
    }
  }

  private def readFace(string: String, vertices: List[Vector3f], texCoords: List[Vector2f],
                      normals: List[Vector3f]): Unit = {
    val vertList = string.split("\\s+").map(vertex => {
      vertex.split("/").toList match {
        case (vert: String) :: Nil =>
          val v: Vector3f = getValue(vert.toInt, vertices)
          new Vertex(v, None, None)
        case (vert: String) :: (tex: String) :: Nil =>
          val v: Vector3f = getValue(vert.toInt, vertices)
          val vt: Vector2f = getValue(tex.toInt, texCoords)
          new Vertex(v, Some(vt), None)
        case (vert: String) :: (vt: String) :: (norm: String) :: Nil if vt.isEmpty =>
          val v: Vector3f = getValue(vert.toInt, vertices)
          val vn: Vector3f = getValue(norm.toInt, normals)
          new Vertex(v, None, Some(vn))
        case (vert: String) :: (tex: String) :: (norm: String) :: Nil =>
          val v: Vector3f = getValue(vert.toInt, vertices)
          val vt: Vector2f = getValue(tex.toInt, texCoords)
          val vn: Vector3f = getValue(norm.toInt, normals)
          new Vertex(v, Some(vt), Some(vn))
      }
    }).toList
    if (vertList.size > 4 || vertList.size < 2) {
      System.err.println("Edge or polygon detected in OBJ. Ignored.")
      return
    }
    val f = new Face(vertList)
    if (matFaces.nonEmpty) {
      matFaces += (currentMatName -> (f :: matFaces(currentMatName)))
    } else {
      faces = f :: faces
    }
  }

  private def getValue[A](value: Int, list: List[A]): A = {
    var tempValue: Int = value
    if (tempValue < 0) {
      tempValue = list.size + tempValue + 1
    }
    list(tempValue - 1)
  }
}
