package tc

import java.util.Date

abstract class FetchInterval
case class FetchPastWeek() extends FetchInterval
case class FetchCustom( startDate: Date, endDate: Date )

class FetchArgs {
    val runDate = new Date()
	val fetchInterval : FetchInterval = new FetchPastWeek

	def fmtDate( dt: Date ) = String.format( "%tD", dt )

	val fmtInterval = fetchInterval match {
		case FetchPastWeek() => "week ending " + fmtDate( runDate )
		case _ => "fmtInterval: Unmatched interval format"
	}
}

class FetchResult( args: FetchArgs, pf: String, wpf: String ) {
	val previewFile = pf
    val wpFile = wpf
}


