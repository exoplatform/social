function Clock() { }

Clock.prototype.startClock = function() {
  var d_names = gadgets.i18n.DateTimeConstants.STANDALONESHORTWEEKDAYS;
  var m_names = gadgets.i18n.DateTimeConstants.STANDALONESHORTMONTHS;
  var a_ps = gadgets.i18n.DateTimeConstants.AMPMS;
  var clockEl = _gel("Clock");
  var date = new Date();
  var a_p = "";
  var curr_hour = date.getHours();
  var curr_min = date.getMinutes();
  var curr_sec = date.getSeconds();

  a_p = (curr_hour < 12) ? a_ps[0] : a_ps[1];
  curr_hour = ( curr_hour > 12 ) ? curr_hour - 12 : curr_hour;
  curr_hour = ( curr_hour == 0 ) ? 12 : curr_hour;
  curr_hour = (curr_hour < 10) ? '0' + curr_hour : curr_hour;
  curr_min = (curr_min < 10) ? '0' + curr_min : curr_min;
  curr_sec = (curr_sec < 10) ? '0' + curr_sec : curr_sec;

  var curr_time = (curr_hour + ":" + curr_min + ":" + curr_sec);
  var day = date.getDate();
  var today= d_names[date.getDay()];
  var month=m_names[date.getMonth()];

  var html = "";
  html += '<div class="UIClock">';
  html += '	<div class="ClockBGL">';
  html += '		<div class="ClockBGR">';
  html += '			<div class="ClockBGM">';
  html += '				<div class="Meridian">' + a_p + '</div>';
  html += '				<div class="Time">' + curr_time + '</div>';
  html += '				<div class="Day">' + today + '</div>';
  html += '				<div class="Month">' + day + ', ' + month + '</div>';
  html += '				<div class="ClearLeft"><span></span></div>';
  html += '			</div>';
  html += '		</div>';
  html += '	</div>';
  html += '</div>';

  clockEl.innerHTML = html;
}

var clock = new Clock();
setInterval(function() {clock.startClock();}, 1000);
