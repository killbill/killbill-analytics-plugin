function Reports() {
    this.protocol = 'http';
    this.host = '127.0.0.1';
    this.port = 8080;
    this.basePath = '/plugins/killbill-analytics/static/analytics.html';

    this.startDate = moment().subtract('months', 3).format('YYYY[-]MM[-]DD');
    this.endDate = moment().format('YYYY[-]MM[-]DD');
    // Map position -> report names
    this.reports = {};
    // Map position -> smoothing function
    this.smoothingFunctions = {};
}

Reports.prototype.init = function() {
    var url = $.url();
    if (url.attr('host') && url.attr('port')) {
        this.protocol = url.attr('protocol');
        this.host = url.attr('host');
        this.port = url.attr('port');
    }
    this.basePath = url.attr('path');

    var params = url.param();
    for (var key in params) {
        if (key == 'startDate') {
            this.startDate = params[key];
        } else if (key == 'endDate') {
            this.endDate = params[key];
        } else if (key.startsWith('smooth')) {
            var position = key.split('smooth')[1];
            this.smoothingFunctions[position] = params[key];
        } else if (key.startsWith('report')) {
            var position = key.split('report')[1];
            if (!(params[key] instanceof Array)) {
                this.reports[position] = [params[key]];
            } else {
                this.reports[position] = params[key];
            }
        }
    }
}

Reports.prototype.hasReport = function(val) {
    var found = false;

    var that = this;
    $.each(Object.keys(this.reports), function(i, position) {
        $.each(that.reports[position], function(j, reportName) {
            if (reportName === val) {
                found = true;
                return;
            }
        });
    });

    return found;
}

Reports.prototype.availableReports = function(callback) {
    var url = this.protocol + '://' + this.host + ':' + this.port + '/plugins/killbill-analytics/reports';
    $.get(url, function(allReports) { callback(allReports); }, 'json');
}

Reports.prototype.getDataForReport = function(position, callback) {
    var url = this.protocol + '://' + this.host + ':' + this.port + '/plugins/killbill-analytics/reports';
    url += '?startDate=' + this.startDate;
    url += '&endDate=' + this.endDate;
    url += '&name=' + this.reports[position].join('&name=');
    if (this.smoothingFunctions[position]) {
        url += '&smooth=' + this.smoothingFunctions[position]
    }

    return $.ajax({
        type: 'GET',
        contentType: 'application/json',
        dataType: 'json',
        url: url,
        beforeSend: function(jqXHR, settings) {
            // Display the loading indicator
            $('#loading-spinner').spin({
                top: '150px',
                lines: 10,
                length: 8,
                width: 4,
                radius: 8
            });
        }
    }).done(function(data) {
                callback(position, data);
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.responseText) {
                    try {
                        errors = $.parseJSON(jqXHR.responseText);
                        if (errors['message']) {
                            displayError('Error generating report nb. ' + position + ':\n' + errors['message']);
                        } else {
                            displayError('Error generating report nb. ' + position + ':\n' + errors);
                        }
                    } catch (err) {
                        displayError('Error generating report nb. ' + position + ':\n' + jqXHR.responseText);
                    }
                } else {
                    if (errorThrown) {
                        displayError('Error generating report nb. ' + position + ':\n' + errorThrown);
                    } else {
                        displayError('Error generating report nb. ' + position + ':\n' + textStatus + ' (status '+ jqXHR.status + ')');
                    }
                }
    }).always(function(jqXHR, textStatus, errorThrown) {
        // Hide the loading indicator
        $('#loading-spinner').spin(false);
    });
}

Reports.prototype.getDataForReports = function(callback) {
    // Array of all deferreds
    var futures = []
    // Array of all the data, the index being the report position (starts at zero)
    var futuresData = new Array(Object.keys(this.reports).length);

    for (var position in this.reports) {
        // Fetch the data
        var future = this.getDataForReport(position, function(zePosition, reportsData) {
            if (!(reportsData instanceof Array) || reportsData.length == 0) {
                futuresData[zePosition - 1] = { "name": "No data", "values": [] };
            } else {
                log.trace(reportsData);
                futuresData[zePosition - 1] = reportsData[0];
            }
        });
        futures.push(future);
    }

    // Apply callback on join
    $.when.apply(null, futures).done(function() { callback(futuresData); });
}

Reports.prototype.buildRefreshURL = function(newReports, newStartDate, newEndDate) {
    if ($.isEmptyObject(newReports)) {
        // No change in reports - we make sure to keep the ordering though
        newReports = this.reports;
    }

    var url = this.protocol + '://' + this.host + ':' + this.port + this.basePath;
    url += '?startDate=' + (newStartDate ? newStartDate : this.startDate);
    url += '&endDate=' + (newEndDate ? newEndDate : this.endDate);

    var i = 0;
    for (var position in newReports) {
      if (i >= 1) {
        url += '&';
      }
      i += 1;

      url += 'report' + position + '=' + newReports[reportName];
    }

    return url;
};