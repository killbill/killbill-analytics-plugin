$(document).ready(function() {
    var reports = new Reports();
    reports.init();

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
    $('#start-date').datepicker('setDate', reports.startDate);
    $('#end-date').datepicker('setDate', reports.endDate);

    // Populate the dashboard builder drop down with the available reports
    reports.availableReports(function(allReports) {
        $.each(allReports, function(i, report) {
            var input = $('<input>').attr('type', 'checkbox')
                                    .attr('value', report.reportName)
                                    .attr('id', 'report' + i);

            // Currently displayed?
            if (reports.hasReport(report.reportName)) {
                input.attr('checked','checked');
            }

            var label = $('<label>').append(input).append(report.reportPrettyName);
            var li = $('<li>').attr('class', 'checkbox').append(label);
            $('#custom-dashboard-builder').append(li);
        });
    });

    // Configure the refresh button callback
    $('#refresh-graphs').click(function() {
      var newReports = {}
      $.map($('#custom-dashboard-builder input:checked'), function(newReport, idx) {
          newReports[idx + 1] = newReport.value;
      });

      var startDatepicker = $('#start-date').data('datepicker');
      var newStartDate = dateFromDatepicker(startDatepicker);

      var endDatepicker = $('#end-date').data('datepicker');
      var newEndDate = dateFromDatepicker(endDatepicker);

      $(location).attr('href', reports.buildRefreshURL(newReports, newStartDate, newEndDate));
    });

    // Finally, draw the graphs
    reports.getDataForReports(function(dataForAllReports) {
        var input = new killbillGraph.KBInputGraphs(800, 400, 80, 80, 80, 80, 160, dataForAllReports);
        drawAll(input);

        // Build the data tables
        // TODO (STEPH) Check story for data points
        //buildDataTables(reports, dataForAllReports, from, to, smoothFunctions);
    });
});

//
// Utils
//

function displayError(msg) {
    $('#alert-error').html(msg);
    $('#alert-error').show();
}

// http://stackoverflow.com/questions/646628/javascript-startswith
if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function(str) {
        return this.slice(0, str.length) == str;
    };
}
