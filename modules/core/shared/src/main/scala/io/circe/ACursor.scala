package io.circe

import cats.Applicative
import cats.kernel.Eq
import scala.annotation.tailrec

/**
 * A zipper that represents a position in a JSON document and supports navigation and modification.
 *
 * The `focus` represents the current position of the cursor; it may be updated with `withFocus` or
 * changed using navigation methods like `left` and `right`.
 *
 * @groupname Utilities Miscellaneous utilities
 * @groupprio Utilities 0
 * @groupname Access Access and navigation
 * @groupprio Access 1
 * @groupname Modification Modification
 * @groupprio Modification 2
 * @groupname ArrayAccess Array access
 * @groupprio ArrayAccess 3
 * @groupname ObjectAccess Object access
 * @groupprio ObjectAccess 4
 * @groupname ArrayNavigation Array navigation
 * @groupprio ArrayNavigation 5
 * @groupname ObjectNavigation Object navigation
 * @groupprio ObjectNavigation 6
 * @groupname ArrayModification Array modification
 * @groupprio ArrayModification 7
 * @groupname ObjectModification Object modification
 * @groupprio ObjectModification 8
 * @groupname Decoding Decoding
 * @groupprio Decoding 9
 *
 * @author Travis Brown
 */
sealed abstract class ACursor {
  /**
   * The current location in the document.
   *
   * @group Access
   */
  def focus: Option[Json]

  /**
   * The operations that have been performed so far.
   *
   * @group Decoding
   */
  def history: List[CursorOp]

  /**
   * Indicate whether this cursor represents the result of a successful
   * operation.
   *
   * @group Decoding
   */
  def succeeded: Boolean

  /**
   * Indicate whether this cursor represents the result of an unsuccessful
   * operation.
   *
   * @group Decoding
   */
  final def failed: Boolean = !succeeded

  /**
   * Return the cursor as an [[HCursor]] if it was successful.
   *
   * @group Decoding
   */
  def success: Option[HCursor]

  /**
   * Return to the root of the document.
   *
   * @group Access
   */
  def top: Option[Json]

  /**
   * Modify the focus using the given function.
   *
   * @group Modification
   */
  def withFocus(f: Json => Json): ACursor

  /**
   * Modify the focus in a context using the given function.
   *
   * @group Modification
   */
  def withFocusM[F[_]](f: Json => F[Json])(implicit F: Applicative[F]): F[ACursor]

  /**
   * Replace the focus.
   *
   * @group Modification
   */
  final def set(j: Json): ACursor = withFocus(_ => j)

  /**
   * If the focus is a JSON array, return the elements to the left.
   *
   * @group ArrayAccess
   */
  def lefts: Option[List[Json]]

  /**
   * If the focus is a JSON array, return the elements to the right.
   *
   * @group ArrayAccess
   */
  def rights: Option[List[Json]]

  /**
   * If the focus is a JSON object, return its field names in a set.
   *
   * @group ObjectAccess
   */
  def fieldSet: Option[Set[String]]

  /**
   * If the focus is a JSON object, return its field names in their original order.
   *
   * @group ObjectAccess
   */
  def fields: Option[List[String]]

  /**
   * Delete the focus and move to its parent.
   *
   * @group Modification
   */
  def delete: ACursor

  /**
   * Move the focus to the parent.
   *
   * @group Access
   */
  def up: ACursor

  /**
   * If the focus is an element in a JSON array, move to the left.
   *
   * @group ArrayNavigation
   */
  def left: ACursor

  /**
   * If the focus is an element in a JSON array, move to the right.
   *
   * @group ArrayNavigation
   */
  def right: ACursor

  /**
   * If the focus is an element in a JSON array, move to the first element.
   *
   * @group ArrayNavigation
   */
  def first: ACursor

  /**
   * If the focus is an element in a JSON array, move to the last element.
   *
   * @group ArrayNavigation
   */
  def last: ACursor

  /**
   * If the focus is an element in JSON array, move to the left the given number of times.
   *
   * A negative value will move the cursor right.
   *
   * @group ArrayNavigation
   */
  def leftN(n: Int): ACursor

  /**
   * If the focus is an element in JSON array, move to the right the given number of times.
   *
   * A negative value will move the cursor left.
   *
   * @group ArrayNavigation
   */
  def rightN(n: Int): ACursor

  /**
   * If the focus is an element in a JSON array, move to the left until the given predicate matches
   * the new focus.
   *
   * @group ArrayNavigation
   */
  def leftAt(p: Json => Boolean): ACursor

  /**
   * If the focus is an element in a JSON array, move to the right until the given predicate matches
   * the new focus.
   *
   * @group ArrayNavigation
   */
  def rightAt(p: Json => Boolean): ACursor

  /**
   * If the focus is an element in a JSON array, find the first element at or to its right that
   * matches the given predicate.
   *
   * @group ArrayNavigation
   */
  def find(p: Json => Boolean): ACursor

  /**
   * If the focus is a JSON array, move to its first element.
   *
   * @group ArrayNavigation
   */
  def downArray: ACursor

  /**
   * If the focus is a JSON array, move to the first element that satisfies the given predicate.
   *
   * @group ArrayNavigation
   */
  def downAt(p: Json => Boolean): ACursor

  /**
   * If the focus is a JSON array, move to the element at the given index.
   *
   * @group ArrayNavigation
   */
  def downN(n: Int): ACursor

  /**
   * If the focus is a value in a JSON object, move to a sibling with the given key.
   *
   * @group ObjectNavigation
   */
  def field(k: String): ACursor

  /**
   * If the focus is a JSON object, move to the value of the given key.
   *
   * @group ObjectNavigation
   */
  def downField(k: String): ACursor

  /**
   * Delete the focus and move to the left in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteGoLeft: ACursor

  /**
   * Delete the focus and move to the right in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteGoRight: ACursor

  /**
   * Delete the focus and move to the first element in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteGoFirst: ACursor

  /**
   * Delete the focus and move to the last element in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteGoLast: ACursor

  /**
   * Delete all values to the left of the focus in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteLefts: ACursor

  /**
   * Delete all values to the right of the focus in a JSON array.
   *
   * @group ArrayModification
   */
  def deleteRights: ACursor

  /**
   * Replace all values to the left of the focus in a JSON array.
   *
   * @group ArrayModification
   */
  def setLefts(x: List[Json]): ACursor

  /**
   * Replace all values to the right of the focus in a JSON array.
   *
   * @group ArrayModification
   */
  def setRights(x: List[Json]): ACursor

  /**
   * Delete the focus and move to the sibling with the given key in a JSON object.
   *
   * @group ObjectModification
   */
  def deleteGoField(k: String): ACursor

  /**
   * Attempt to decode the focus as an `A`.
   *
   * @group Decoding
   */
  final def as[A](implicit d: Decoder[A]): Decoder.Result[A] = d.tryDecode(this)

  /**
   * Attempt to decode the value at the given key in a JSON object as an `A`.
   *
   * @group Decoding
   */
  final def get[A](k: String)(implicit d: Decoder[A]): Decoder.Result[A] = downField(k).as[A]

  /**
   * Attempt to decode the value at the given key in a JSON object as an `A`.
   * If the field `k` is missing, then use the `fallback` instead.
   *
   * @group Decoding
   */
  final def getOrElse[A](k: String)(fallback: => A)(implicit d: Decoder[A]): Decoder.Result[A] =
    get[Option[A]](k) match {
      case Right(Some(a)) => Right(a)
      case Right(None) => Right(fallback)
      case l @ Left(_) => l.asInstanceOf[Decoder.Result[A]]
    }

  /**
   * Replay an operation against this cursor.
   *
   * @group Utilities
   */
  final def replayOne(op: CursorOp): ACursor = op match {
    case CursorOp.MoveLeft => left
    case CursorOp.MoveRight => right
    case CursorOp.MoveFirst => first
    case CursorOp.MoveLast => last
    case CursorOp.MoveUp => up
    case CursorOp.LeftN(n) => leftN(n)
    case CursorOp.RightN(n) => rightN(n)
    case CursorOp.LeftAt(p) => leftAt(p)
    case CursorOp.RightAt(p) => rightAt(p)
    case CursorOp.Find(p) => find(p)
    case CursorOp.Field(k) => field(k)
    case CursorOp.DownField(k) => downField(k)
    case CursorOp.DownArray => downArray
    case CursorOp.DownAt(p) => downAt(p)
    case CursorOp.DownN(n) => downN(n)
    case CursorOp.DeleteGoParent => delete
    case CursorOp.DeleteGoLeft => deleteGoLeft
    case CursorOp.DeleteGoRight => deleteGoRight
    case CursorOp.DeleteGoFirst => deleteGoFirst
    case CursorOp.DeleteGoLast => deleteGoLast
    case CursorOp.DeleteGoField(k) => deleteGoField(k)
    case CursorOp.DeleteLefts => deleteLefts
    case CursorOp.DeleteRights => deleteRights
    case CursorOp.SetLefts(js) => setLefts(js)
    case CursorOp.SetRights(js) => setRights(js)
  }

  /**
   * Replay history (a list of operations in reverse "chronological" order) against this cursor.
   *
   * @group Utilities
   */
  final def replay(history: List[CursorOp]): ACursor = history.foldRight(this)((op, c) => c.replayOne(op))
}

final object ACursor {
  implicit val eqACursor: Eq[ACursor] = Eq.instance {
    case (a: HCursor, b: HCursor) => HCursor.eqHCursor.eqv(a, b)
    case (a: FailedCursor, b: FailedCursor) => FailedCursor.eqFailedCursor.eqv(a, b)
    case _ => false
  }
}

sealed abstract class FailedCursor(final val incorrectFocus: Boolean) extends ACursor {
  final def succeeded: Boolean = false
  final def success: Option[HCursor] = None

  final def focus: Option[Json] = None
  final def top: Option[Json] = None

  final def withFocus(f: Json => Json): ACursor = this
  final def withFocusM[F[_]](f: Json => F[Json])(implicit F: Applicative[F]): F[ACursor] = F.pure(this)

  final def fieldSet: Option[Set[String]] = None
  final def fields: Option[List[String]] = None
  final def lefts: Option[List[Json]] = None
  final def rights: Option[List[Json]] = None

  final def downArray: ACursor = this
  final def downAt(p: Json => Boolean): ACursor = this
  final def downField(k: String): ACursor = this
  final def downN(n: Int): ACursor = this
  final def find(p: Json => Boolean): ACursor = this
  final def leftAt(p: Json => Boolean): ACursor = this
  final def leftN(n: Int): ACursor = this
  final def rightAt(p: Json => Boolean): ACursor = this
  final def rightN(n: Int): ACursor = this
  final def up: ACursor = this

  final def left: ACursor = this
  final def right: ACursor = this
  final def first: ACursor = this
  final def last: ACursor = this

  final def delete: ACursor = this
  final def deleteGoLeft: ACursor = this
  final def deleteGoRight: ACursor = this
  final def deleteGoFirst: ACursor = this
  final def deleteGoLast: ACursor = this
  final def deleteLefts: ACursor = this
  final def deleteRights: ACursor = this

  final def setLefts(x: List[Json]): ACursor = this
  final def setRights(x: List[Json]): ACursor = this

  final def field(k: String): ACursor = this
  final def deleteGoField(q: String): ACursor = this
}

final object FailedCursor {
  implicit val eqFailedCursor: Eq[FailedCursor] =
    cats.instances.list.catsKernelStdEqForList[CursorOp].on[FailedCursor](_.history)
}

sealed abstract class HCursor extends ACursor {
  def value: Json
  protected def lastCursor: HCursor
  protected def lastOp: CursorOp

  final def history: List[CursorOp] = {
    var next = this
    val builder = List.newBuilder[CursorOp]

    while (next.ne(null)) {
      if (next.lastOp.ne(null)) {
        builder += next.lastOp
      }
      next = next.lastCursor
    }

    builder.result()
  }

  final def succeeded: Boolean = true
  final def success: Option[HCursor] = Some(this)

  final def focus: Option[Json] = Some(value)

  final def fieldSet: Option[Set[String]] = value match {
    case Json.JObject(o) => Some(o.fieldSet)
    case _ => None
  }

  final def fields: Option[List[String]] = value match {
    case Json.JObject(o) => Some(o.fields)
    case _ => None
  }

  final def leftN(n: Int): ACursor = if (n < 0) rightN(-n) else {
    @tailrec
    def go(i: Int, c: ACursor): ACursor = if (i == 0) c else go(i - 1, c.left)

    go(n, this)
  }

  final def rightN(n: Int): ACursor = if (n < 0) leftN(-n) else {
    @tailrec
    def go(i: Int, c: ACursor): ACursor = if (i == 0) c else go(i - 1, c.right)

    go(n, this)
  }

  final def leftAt(p: Json => Boolean): ACursor = {
    @tailrec
    def go(c: ACursor): ACursor = c match {
      case success: HCursor => if (p(success.value)) success else go(success.left)
      case other => other
    }

    go(left)
  }

  final def rightAt(p: Json => Boolean): ACursor = right.find(p)

  final def find(p: Json => Boolean): ACursor = {
    @annotation.tailrec
    def go(c: ACursor): ACursor = c match {
      case success: HCursor => if (p(success.value)) success else go(success.right)
      case other => other
    }

    go(this)
  }

  final def downAt(p: Json => Boolean): ACursor = downArray.find(p)

  final def downN(n: Int): ACursor = downArray.rightN(n)

  /**
   * Create a new cursor that has failed on the given operation.
   *
   * @group Utilities
   */
  protected def fail(op: CursorOp): ACursor
}

final object HCursor {
  def fromJson(value: Json): HCursor = new ValueCursor(value) {
    protected def lastCursor: HCursor = null
    protected def lastOp: CursorOp = null
  }

  private[this] val eqJsonList: Eq[List[Json]] = cats.instances.list.catsKernelStdEqForList[Json]

  implicit val eqHCursor: Eq[HCursor] = new Eq[HCursor] {
    def eqv(a: HCursor, b: HCursor): Boolean =
      Json.eqJson.eqv(a.value, b.value) &&
      ((a.lastOp.eq(null) && b.lastOp.eq(null)) || CursorOp.eqCursorOp.eqv(a.lastOp, b.lastOp)) &&
      ((a.lastCursor.eq(null) && b.lastCursor.eq(null)) || eqv(a.lastCursor, b.lastCursor)) && (
        (a, b) match {
          case (_: ValueCursor, _: ValueCursor) => true
          case (aa: ArrayCursor, ba: ArrayCursor) =>
            aa.changed == ba.changed &&
            eqv(aa.parent, ba.parent) &&
            eqJsonList.eqv(aa.ls, ba.ls) &&
            eqJsonList.eqv(aa.rs, ba.rs)
          case (ao: ObjectCursor, bo: ObjectCursor) =>
            ao.changed == bo.changed &&
            eqv(ao.parent, bo.parent) &&
            ao.key == bo.key &&
            JsonObject.eqJsonObject.eqv(ao.obj, bo.obj)
          case _ => false
        }
      )
  }

  private[this] sealed abstract class BaseHCursor extends HCursor { self =>
    final def top: Option[Json] = {
      @tailrec
      def go(c: HCursor): Json = c match {
        case v: ValueCursor => v.value
        case a: ArrayCursor =>
          val newValue = Json.fromValues((a.value :: a.rs).reverse_:::(a.ls))

          go(
            a.parent match {
              case pv: ValueCursor => new ValueCursor(newValue) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
              case pa: ArrayCursor => new ArrayCursor(newValue, pa.parent, a.changed || pa.changed, pa.ls, pa.rs) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
              case po: ObjectCursor => new ObjectCursor(
                newValue,
                po.parent,
                a.changed || po.changed,
                po.key,
                if (a.changed) po.obj.add(po.key, newValue) else po.obj
              ) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
            }
          )
        case o: ObjectCursor =>
          val newValue = Json.fromJsonObject(if (o.changed) o.obj.add(o.key, o.value) else o.obj)

          go(
            o.parent match {
              case pv: ValueCursor => new ValueCursor(newValue) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
              case pa: ArrayCursor => new ArrayCursor(newValue, pa.parent, o.changed || pa.changed, pa.ls, pa.rs) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
              case po: ObjectCursor => new ObjectCursor(
                newValue,
                po.parent,
                o.changed || po.changed,
                po.key,
                po.obj
              ) {
                protected def lastCursor: HCursor = null
                protected def lastOp: CursorOp = null
              }
            }
          )
      }

      Some(go(this))
    }

    final def downArray: ACursor = value match {
      case Json.JArray(h :: t) =>
        new ArrayCursor(h, this, false, Nil, t) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DownArray
        }
      case _ => fail(CursorOp.DownArray)
    }

    final def downField(k: String): ACursor = value match {
      case Json.JObject(o) =>
        val m = o.toMap

        if (!m.contains(k)) fail(CursorOp.DownField(k)) else {
          new ObjectCursor(m(k), this, false, k, o) {
            protected def lastCursor: HCursor = self
            protected def lastOp: CursorOp = CursorOp.DownField(k)
          }
        }
      case _ => fail(CursorOp.DownField(k))
    }

    protected final def fail(op: CursorOp): ACursor =
      new FailedCursor((op.requiresObject && !value.isObject) || (op.requiresArray && !value.isArray)) {
        def history: List[CursorOp] = op :: self.history
      }
  }

  private[this] abstract class ValueCursor(final val value: Json) extends BaseHCursor { self =>
    final def withFocus(f: Json => Json): ACursor = new ValueCursor(f(value)) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = null
    }

    final def withFocusM[F[_]](f: Json => F[Json])(implicit F: Applicative[F]): F[ACursor] =
      F.map(f(value))(newValue =>
        new ValueCursor(newValue) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = null
        }
      )

    final def lefts: Option[List[Json]] = None
    final def rights: Option[List[Json]] = None

    final def up: ACursor = fail(CursorOp.MoveUp)
    final def delete: ACursor = fail(CursorOp.DeleteGoParent)

    final def left: ACursor = fail(CursorOp.MoveLeft)
    final def right: ACursor = fail(CursorOp.MoveRight)
    final def first: ACursor = fail(CursorOp.MoveFirst)
    final def last: ACursor = fail(CursorOp.MoveLast)

    final def deleteGoLeft: ACursor = fail(CursorOp.DeleteGoLeft)
    final def deleteGoRight: ACursor = fail(CursorOp.DeleteGoRight)
    final def deleteGoFirst: ACursor = fail(CursorOp.DeleteGoFirst)
    final def deleteGoLast: ACursor = fail(CursorOp.DeleteGoLast)
    final def deleteLefts: ACursor = fail(CursorOp.DeleteLefts)
    final def deleteRights: ACursor = fail(CursorOp.DeleteRights)

    final def setLefts(x: List[Json]): ACursor = fail(CursorOp.SetLefts(x))
    final def setRights(x: List[Json]): ACursor = fail(CursorOp.SetRights(x))

    final def field(k: String): ACursor = fail(CursorOp.Field(k))
    final def deleteGoField(k: String): ACursor = fail(CursorOp.DeleteGoField(k))
  }

  private[this] abstract class ArrayCursor(
    final val value: Json,
    final val parent: HCursor,
    final val changed: Boolean,
    final val ls: List[Json],
    final val rs: List[Json]
  ) extends BaseHCursor { self =>
    final def withFocus(f: Json => Json): ACursor = new ArrayCursor(f(value), parent, changed = true, ls, rs) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = null
    }

    final def withFocusM[F[_]](f: Json => F[Json])(implicit F: Applicative[F]): F[ACursor] =
      F.map(f(value))(newValue =>
        new ArrayCursor(newValue, parent, changed = true, ls, rs) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = null
        }
      )

    final def up: ACursor = {
      val newValue = Json.fromValues((value :: rs).reverse_:::(ls))

      parent match {
        case v: ValueCursor =>  new ValueCursor(newValue) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
        case a: ArrayCursor => new ArrayCursor(newValue, a.parent, changed || a.changed, a.ls, a.rs) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
        case o: ObjectCursor => new ObjectCursor(
          newValue,
          o.parent,
          changed || o.changed,
          o.key,
          if (changed) o.obj.add(o.key, newValue) else o.obj
        ) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
      }
    }

    final def delete: ACursor = {
      val newValue = Json.fromValues(rs.reverse_:::(ls))

      parent match {
        case v: ValueCursor => new ValueCursor(newValue) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
        case a: ArrayCursor => new ArrayCursor(newValue, a.parent, true, a.ls, a.rs) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
        case o: ObjectCursor => new ObjectCursor(
          newValue,
          o.parent,
          true,
          o.key,
          o.obj
        ) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
      }
    }

    final def lefts: Option[List[Json]] = Some(ls)
    final def rights: Option[List[Json]] = Some(rs)

    final def left: ACursor = ls match {
      case h :: t => new ArrayCursor(h, parent, changed, t, value :: rs) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.MoveLeft
      }
      case Nil => fail(CursorOp.MoveLeft)
    }

    final def right: ACursor = rs match {
      case h :: t => new ArrayCursor(h, parent, changed, value :: ls, t) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.MoveRight
      }
      case Nil => fail(CursorOp.MoveRight)
    }

    final def first: ACursor = (value :: rs).reverse_:::(ls) match {
      case h :: t => new ArrayCursor(h, parent, changed, Nil, t) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.MoveFirst
      }
      case Nil => fail(CursorOp.MoveFirst)
    }

    final def last: ACursor = (value :: ls).reverse_:::(rs) match {
      case h :: t => new ArrayCursor(h, parent, changed, t, Nil) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.MoveLast
      }
      case Nil => fail(CursorOp.MoveLast)
    }

    final def deleteGoLeft: ACursor = ls match {
      case h :: t => new ArrayCursor(h, parent, true, t, rs) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.DeleteGoLeft
      }
      case Nil => fail(CursorOp.DeleteGoLeft)
    }

    final def deleteGoRight: ACursor = rs match {
      case h :: t => new ArrayCursor(h, parent, true, ls, t) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.DeleteGoRight
      }
      case Nil => fail(CursorOp.DeleteGoRight)
    }

    final def deleteGoFirst: ACursor = rs.reverse_:::(ls) match {
      case h :: t => new ArrayCursor(h, parent, true, Nil, t) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.DeleteGoFirst
      }
      case Nil => fail(CursorOp.DeleteGoFirst)
    }

    final def deleteGoLast: ACursor = ls.reverse_:::(rs) match {
      case h :: t => new ArrayCursor(h, parent, true, t, Nil) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.DeleteGoLast
      }
      case Nil => fail(CursorOp.DeleteGoLast)
    }

    final def deleteLefts: ACursor = new ArrayCursor(value, parent, true, Nil, rs) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = CursorOp.DeleteLefts
    }

    final def deleteRights: ACursor = new ArrayCursor(value, parent, true, ls, Nil) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = CursorOp.DeleteRights
    }

    final def setLefts(js: List[Json]): ACursor = new ArrayCursor(value, parent, true, js, rs) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = CursorOp.SetLefts(js)
    }

    final def setRights(js: List[Json]): ACursor = new ArrayCursor(value, parent, true, ls, js) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = CursorOp.SetRights(js)
    }

    final def field(k: String): ACursor = fail(CursorOp.Field(k))
    final def deleteGoField(k: String): ACursor = fail(CursorOp.DeleteGoField(k))
  }

  private[this] sealed abstract class ObjectCursor(
    final val value: Json,
    final val parent: HCursor,
    final val changed: Boolean,
    final val key: String,
    final val obj: JsonObject
  ) extends BaseHCursor { self =>
    final def withFocus(f: Json => Json): ACursor = new ObjectCursor(f(value), parent, true, key, obj) {
      protected def lastCursor: HCursor = self
      protected def lastOp: CursorOp = null
    }

    final def withFocusM[F[_]](f: Json => F[Json])(implicit F: Applicative[F]): F[ACursor] =
      F.map(f(value))(newValue =>
        new ObjectCursor(newValue, parent, true, key, obj) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = null
        }
      )

    final def lefts: Option[List[Json]] = None
    final def rights: Option[List[Json]] = None

    final def up: ACursor = {
      val newValue = Json.fromJsonObject(if (changed) obj.add(key, value) else obj)

      parent match {
        case v: ValueCursor => new ValueCursor(newValue) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
        case a: ArrayCursor => new ArrayCursor(newValue, a.parent, changed || a.changed, a.ls, a.rs) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
        case o: ObjectCursor => new ObjectCursor(newValue, o.parent, changed || o.changed, o.key, o.obj) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.MoveUp
        }
      }
    }

    final def delete: ACursor = {
      val newValue = Json.fromJsonObject(obj.remove(key))

      parent match {
        case v: ValueCursor => new ValueCursor(newValue) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
        case a: ArrayCursor => new ArrayCursor(newValue, a.parent, true, a.ls, a.rs) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
        case o: ObjectCursor => new ObjectCursor(newValue, o.parent, true, o.key, o.obj) {
          protected def lastCursor: HCursor = self
          protected def lastOp: CursorOp = CursorOp.DeleteGoParent
        }
      }
    }

    final def field(k: String): ACursor = {
      val m = obj.toMap

      if (m.contains(k)) new ObjectCursor(m(k), parent, changed, k, obj) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.Field(k)
      } else fail(CursorOp.Field(k))
    }

    final def deleteGoField(k: String): ACursor = {
      val m = obj.toMap

      if (m.contains(k)) new ObjectCursor(m(k), parent, true, k, obj.remove(key)) {
        protected def lastCursor: HCursor = self
        protected def lastOp: CursorOp = CursorOp.DeleteGoField(k)
      } else fail(CursorOp.DeleteGoField(k))
    }

    final def left: ACursor = fail(CursorOp.MoveLeft)
    final def right: ACursor = fail(CursorOp.MoveRight)
    final def first: ACursor = fail(CursorOp.MoveFirst)
    final def last: ACursor = fail(CursorOp.MoveLast)

    final def deleteGoLeft: ACursor = fail(CursorOp.DeleteGoLeft)
    final def deleteGoRight: ACursor = fail(CursorOp.DeleteGoRight)
    final def deleteGoFirst: ACursor = fail(CursorOp.DeleteGoFirst)
    final def deleteGoLast: ACursor = fail(CursorOp.DeleteGoLast)
    final def deleteLefts: ACursor = fail(CursorOp.DeleteLefts)
    final def deleteRights: ACursor = fail(CursorOp.DeleteRights)

    final def setLefts(x: List[Json]): ACursor = fail(CursorOp.SetLefts(x))
    final def setRights(x: List[Json]): ACursor = fail(CursorOp.SetRights(x))
  }
}
