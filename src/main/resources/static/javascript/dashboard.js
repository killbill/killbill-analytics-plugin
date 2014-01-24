$(document).ready(function() {
  /**
   * https://github.com/twbs/bootstrap/issues/2097
   */
  $('.dropdown-menu').on('click', function(e){
      if ($(this).hasClass('dropdown-menu-form')){
          e.stopPropagation();
      }
  });

  // Populate the dashboard builder drop down with the available reports
  var currentUrl = $.url();
  var reportsUrl = currentUrl.attr('protocol') + '://' + currentUrl.attr('host') + ':' + currentUrl.attr('port') + '/plugins/killbill-analytics/reports'
  $.get(reportsUrl, function(reports) {
    $.each(reports, function(i, report) {
      var input = $('<input>').attr('type', 'checkbox')
                              .attr('value', report.reportName)
                              .attr('id', 'report' + i)
      var label = $('<label>').append(input).append(report.reportPrettyName);
      var li = $('<li>').attr('class', 'checkbox').append(label);
      $('#custom-dashboard-builder').append(li);
    });
  }, 'json');

  // Configure the start date date picker
  $('#start-date').datepicker({
      format: "yyyy-mm-dd",
      autoclose: true,
      todayHighlight: true
  });

  // Configure the end date date picker
  $('#end-date').datepicker({
      format: "yyyy-mm-dd",
      autoclose: true,
      todayHighlight: true
  });

  // Configure the refresh button callback
  $('#refresh-graphs').click(function() {
    var newReports = $('#custom-dashboard-builder input:checked');
    if (newReports.length > 0) {
      var currentUrl = $.url();
      var url = currentUrl.attr('protocol') + '://' + currentUrl.attr('host') + ':' + currentUrl.attr('port') + currentUrl.attr('path') + '?';
      $.each(newReports, function(i, report) {
        if (i >= 1) {
          url += '&';
        }
        url += 'report' + (i + 1) + '=' + report.value;
      });
    } else {
      var url = $(location).attr('href');
    }

    var startDatepicker = $('#start-date').data('datepicker');
    if (startDatepicker && startDatepicker.dates.length > 0) {
      var startDate = startDatepicker.getDate();
      var startDateString = moment(startDate).format('YYYY[-]MM[-]DD');
      url = updateURLParameter(url, 'startDate', startDateString);
    }

    var endDatepicker = $('#end-date').data('datepicker');
    if (endDatepicker && endDatepicker.dates.length > 0) {
      var endDate = endDatepicker.getDate();
      var endDateString = endDate.getFullYear() + '-' + (endDate.getMonth() + 1) + '-' + endDate.getDate();
      url = updateURLParameter(url, 'endDate', endDateString);
    }

    $(location).attr('href', url);
  });
});

/**
 * http://stackoverflow.com/a/10997390/11236
 */
function updateURLParameter(url, param, paramVal) {
    var TheAnchor = null;
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var additionalURL = tempArray[1];
    var temp = "";

    if (additionalURL) {
        var tmpAnchor = additionalURL.split("#");
        var TheParams = tmpAnchor[0];
            TheAnchor = tmpAnchor[1];
        if (TheAnchor) {
            additionalURL = TheParams;
        }

        tempArray = additionalURL.split("&");

        for (i=0; i<tempArray.length; i++) {
            if(tempArray[i].split('=')[0] != param){
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    }
    else {
        var tmpAnchor = baseURL.split("#");
        var TheParams = tmpAnchor[0];
            TheAnchor  = tmpAnchor[1];

        if (TheParams) {
            baseURL = TheParams;
        }
    }

    if (TheAnchor) {
        paramVal += "#" + TheAnchor;
    }

    var rows_txt = temp + "" + param + "=" + paramVal;
    return baseURL + "?" + newAdditionalURL + rows_txt;
}