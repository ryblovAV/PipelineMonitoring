package monitoring.domain

import monitoring.domain.Connection.ConnectionType

final case class Connection(connectionType: ConnectionType.Value)

object Connection {
  object ConnectionType extends Enumeration {
    val input, output = Value
  }
}
