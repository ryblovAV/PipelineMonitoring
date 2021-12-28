package app
import cats.syntax.all._
import cats.effect.IO

import scala.util.Try

object Util {
  def runSafe[A](io: IO[A]): Either[String, A] =
    Try(io.unsafeRunSync()).toEither.leftMap(_.getMessage)
}
