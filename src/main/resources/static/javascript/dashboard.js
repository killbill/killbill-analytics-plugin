$(document).ready(function() {
    // https://github.com/twbs/bootstrap/issues/2097
    $('.dropdown-menu').on('click', function(e){
        if ($(this).hasClass('dropdown-menu-form')){
            e.stopPropagation();
        }
    });

    // Configure the start and end date picker
    $('#start-date, #end-date').datepicker({
        format: "yyyy-mm-dd",
        autoclose: true,
        todayHighlight: true
    });

    // Figure out the reports about to be displayed
    var reports = extractReportsFromURL();

    // Populate the dashboard builder drop down with the available reports
    var currentUrl = $.url();
    //var reportsUrl = currentUrl.attr('protocol') + '://' + currentUrl.attr('host') + ':' + currentUrl.attr('port') + '/plugins/killbill-analytics/reports'
    var reportsUrl = 'http://127.0.0.1:8080/plugins/killbill-analytics/reports';
    $.get(reportsUrl, function(allReports) {
        $.each(allReports, function(i, report) {
            var input = $('<input>').attr('type', 'checkbox')
                                    .attr('value', report.reportName)
                                    .attr('id', 'report' + i);

            // Currently displayed?
            if (report.reportName in reports) {
                input.attr('checked','checked');
            }

            var label = $('<label>').append(input).append(report.reportPrettyName);
            var li = $('<li>').attr('class', 'checkbox').append(label);
            $('#custom-dashboard-builder').append(li);
        });
    }, 'json');

    // Configure the refresh button callback
    $('#refresh-graphs').click(function() {
      var newReports = {}
      $.map($('#custom-dashboard-builder input:checked'), function(newReport, idx) {
          newReports[newReport.value] = idx + 1;
      });

      if ($.isEmptyObject(newReports)) {
          // No change in reports - we make sure to keep the ordering too
          newReports = reports;
      }

      var url = buildURL(newReports);
      $(location).attr('href', url);
    });
});

function extractReportsFromURL() {
    var reports = {};
    var idx = 1;
    var url = $.url();
    var params = url.param();
    for (var key in params) {
        if (key.startsWith('report')) {
            reports[params[key]] = idx;
            idx += 1;
        }
    }
    return reports;
}

function buildURL(newReports) {
    var currentUrl = $.url();
    var url = currentUrl.attr('protocol') + '://' + currentUrl.attr('host') + ':' + currentUrl.attr('port') + currentUrl.attr('path') + '?';

    var i = 0;
    for (var reportName in newReports) {
      if (i >= 1) {
        url += '&';
      }
      i += 1;

      url += 'report' + newReports[reportName] + '=' + reportName;
    }

    url = updateURLWithDatepickerDates(url);

    return url;
}

function updateURLWithDatepickerDates(url) {
    var startDatepicker = $('#start-date').data('datepicker');
    url = updateURLWithDatepickerDate(url, startDatepicker, 'startDate');

    var endDatepicker = $('#end-date').data('datepicker');
    url = updateURLWithDatepickerDate(url, endDatepicker, 'endDate');

    return url;
}

function updateURLWithDatepickerDate(url, datepicker, parameter) {
    if (datepicker && datepicker.dates.length > 0) {
      var date = datepicker.getDate();
      var dateString = moment(date).format('YYYY[-]MM[-]DD');
      url = updateURLParameter(url, parameter, dateString);
    }
    return url;
}

// http://stackoverflow.com/a/10997390/11236
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

// http://stackoverflow.com/questions/646628/javascript-startswith
if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function (str){
        return this.slice(0, str.length) == str;
    };
}
