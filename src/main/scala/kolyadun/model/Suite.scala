package kolyadun.model

import java.util.UUID

final case class Suite(id: UUID, tasks: List[Task])
