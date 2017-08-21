package mini.objConverter

import mini.math.{Vector2f, Vector3f}

/**
  * Created by miniwolf on 15-04-2017.
  */
class Vertex(val vertex: Vector3f, val texCoord: Option[Vector2f], val normal: Option[Vector3f],
             var index: Int = 0) {
  def canEqual(other: Any): Boolean = other.isInstanceOf[Vertex]

  override def equals(other: Any): Boolean = other match {
    case that: Vertex =>
      (that canEqual this) &&
        vertex == that.vertex &&
        texCoord == that.texCoord &&
        normal == that.normal
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(vertex, texCoord, normal)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
