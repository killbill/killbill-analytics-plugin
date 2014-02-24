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

            // Add describe link
            var modalBody = $('<div/>').attr('class', 'modal-body');
            var fields = $('<form/>').attr('class', 'form-horizontal').attr('role', 'form');
            $.each(report.schema.fields, function(j, field) {
                // Add dimension/metric selector
                var dimension = $('<input/>').attr('type', 'radio')
                                             .attr('name', field.name)
                                             .attr('id', 'dimension-' + i + '-' + j)
                                             .attr('value', 'dimension');
                var metric = $('<input/>').attr('type', 'radio')
                                          .attr('name', field.name)
                                          .attr('id', 'metric-' + i + '-' + j)
                                          .attr('value', 'metric');
                var radios = $('<div/>').attr('class', 'col-sm-4 controls')
                                        .append($('<label/>').attr('class', 'radio-inline').append(dimension).append('Dimension'))
                                        .append($('<label/>').attr('class', 'radio-inline').append(metric).append('Metric'));
                var group = $('<div/>').attr('class', 'form-group')
                                       .append(radios);

                // Add distinct values, if we have them
                if (field.distinctValues) {
                    var select = $('<select multiple/>').attr('class', 'form-control')
                                                        .attr('id', 'values-' + i + '-' + field.name);
                    $.each(field.distinctValues, function(k, value) {
                        select.append($('<option/>').append(value));
                    });
                    group.append($('<div/>').attr('class', 'col-sm-4 controls').append(select));
                }

                // Finally, add the label
                group.append($('<label/>').attr('class', 'control-label').append(field.name + ' (' + field.dataType + ')'));

                fields.append(group);
            });
            fields.append($('<div/>').attr('class', 'form-group')
                                     .append($('<div/>').attr('class', 'col-sm-8 controls')
                                                        .append($('<textarea/>').attr('rows', '3')
                                                                                .attr('style', 'width: 100%')
                                                                                .attr('id', 'custom-dashboard-builder-url-' + i))));
            modalBody.append($('<h3/>').append(report.reportPrettyName))
                                       .append(fields);
            var modal = $('<div/>').attr('class', 'modal fade')
                                   .attr('id', 'availableReportsDetailsModalWrapper-' + i)
                                   .attr('tabindex', '-1')
                                   .attr('role', 'dialog')
                                   .attr('aria-labelledby', 'Details')
                                   .attr('aria-hidden', 'true')
                                   .append($('<div/>').attr('class', 'modal-dialog')
                                                      .append($('<div/>').attr('class', 'modal-content')
                                                                         .append(modalBody)));
            modal.appendTo("body");
            var detailsLink = $('<a/>').text('(details)').css('cursor', 'pointer');
            detailsLink.click(function() {
                $('#availableReportsDetailsModalWrapper-' + i).modal('toggle');
            });
            label.append('&nbsp;').append(detailsLink);

            // Configure the url builder callback
            $('#availableReportsDetailsModalWrapper-' + i).change(function() {
                var reportsUrl = new ReportsUrls(report.reportName);
                $('#availableReportsDetailsModalWrapper-' + i + ' input:radio:checked').each(function() {
                    var dimensionOrMetric = $(this).val();
                    var column = $(this).attr('name');

                    if (dimensionOrMetric == 'dimension') {
                        reportsUrl.addDimension(column, $('#values-' + i + '-' + column).val());
                    } else if (dimensionOrMetric == 'metric') {
                        reportsUrl.addMetric(column);
                    }
                });
                $('#custom-dashboard-builder-url-' + i).val(reportsUrl.url);
            });

            var li = $('<li>').attr('class', 'checkbox').append(label);
            $('#custom-dashboard-builder').append(li);
        });
    });

    // Configure the refresh button callback
    $('#refresh-graphs').click(function() {
      var newReports = {}
      $.map($('#custom-dashboard-builder input:checked'), function(newReport, idx) {
          // For now, we support only one graph per position via the builder
          newReports[idx + 1] = [newReport.value];
      });

      var startDatepicker = $('#start-date').data('datepicker');
      var newStartDate = dateFromDatepicker(startDatepicker);

      var endDatepicker = $('#end-date').data('datepicker');
      var newEndDate = dateFromDatepicker(endDatepicker);

      $(location).attr('href', reports.buildRefreshURL(newReports, newStartDate, newEndDate));
    });

    // Configure the default graphs
    $('#reset-dashboards').click(function() {
        $(location).attr('href', reports.buildRefreshURL({}));
    });
    $('#standard-analytics-dashboards').click(function() {
        $(location).attr('href', reports.buildRefreshURL(reports.ANALYTICS_REPORTS.reports) + '&__preset=ANALYTICS');
    });
    $('#standard-system-dashboards').click(function() {
        $(location).attr('href', reports.buildRefreshURL(reports.SYSTEM_REPORTS.reports) + '&__preset=SYSTEM');
    });
    // Highlight the menu links
    var preset = $.url().param('__preset');
    if (preset == 'ANALYTICS') {
        $('#standard-analytics-dashboards-wrapper').addClass('active');
    } else if (preset == 'SYSTEM') {
        $('#standard-system-dashboards-wrapper').addClass('active');
    }

    // Display the loading indicator
    var spinOptions = {
        top: '150px',
        lines: 10,
        length: 8,
        width: 4,
        radius: 8,
        speed: 1
    }
    $('#loading-spinner').spin(spinOptions);

    // Finally, draw the graphs
    reports.getDataForReports(function(dataForAllReports) {
        // As a hint the AJAX requests are done, accelerate the spinner
        spinOptions['speed'] = 4;
        $('#loading-spinner').spin(spinOptions);

        try {
            if (dataForAllReports.length == 0) {
                displayInfo("Use the menu to select reports");
            } else {
                var reportsGraphs = new ReportsGraphs(reports);
                reportsGraphs.drawAll(dataForAllReports);
            }
        } finally {
            // Hide the loading indicator
            $('#loading-spinner').spin(false);
        }
    });
});

//
// Utils
//

function dateFromDatepicker(datepicker) {
    if (datepicker && datepicker.dates.length > 0) {
        var date = datepicker.getDate();
        return moment(date).format('YYYY[-]MM[-]DD');
    }
}

function displayInfo(msg) {
    $('#alert-info').html(msg);
    $('#alert-info').show();
}

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
