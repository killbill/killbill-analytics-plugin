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

    // Standard sets of reports
    this.ANALYTICS_REPORTS = {
        reports: {
            1: ['trial_starts_count_daily'],
            2: ['cancellations_count_daily'],
            3: ['active_by_product_term_monthly'],
            4: ['invoices_balance_daily']
        }
    };
    this.SYSTEM_REPORTS = {
        reports: {
            1: ['system_report_payments_per_day'],
            2: ['system_report_notifications_per_queue_name'],
            3: ['system_report_notifications_per_queue_name_late'],
            4: ['system_report_payments'],
            5: ['system_report_control_tag_no_test']
        }
    };

    // Debugging
    this.loadFromFilePath = false;
}

Reports.prototype.init = function() {
    var url = $.url();
    // Infer Kill Bill's address by looking at the current address,
    // except if we're loading the file directly (file:///).
    if (url.attr('protocol') != 'file') {
        this.protocol = url.attr('protocol');
        this.host = url.attr('host');
        this.port = url.attr('port');
    } else {
        this.loadFromFilePath = true;
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
    var url = this.buildBaseURL('/plugins/killbill-analytics/reports');
    $.get(url, function(allReports) { callback(allReports); }, 'json');
}

Reports.prototype.getDataForReport = function(position, callback) {
    var url = this.buildDataURL(position);

    return $.ajax({
        type: 'GET',
        contentType: 'application/json',
        dataType: 'json',
        url: url
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
            if (!(reportsData instanceof Array) || reportsData.length == 0 || reportsData[0].data.length == 0) {
                log.debug('Report at position ' + (zePosition - 1) + ' has not data')
                // Skip, to avoid confusing the graphing library
            } else {
                log.debug('Got data for report at position ' + (zePosition - 1));
                log.trace(reportsData);
                futuresData[zePosition - 1] = reportsData[0];
            }
        });
        futures.push(future);
    }

    // Apply callback on join (and remove skipped reports, with no data)
    $.when.apply(null, futures).done(function() { callback($.grep(futuresData, function(e) { return e; })); });
}

Reports.prototype.buildRefreshURLForNewSmooth = function(position, newSmooth) {
    var newReports = $.extend(true, {}, this);
    newReports.smoothingFunctions[position] = newSmooth;
    return newReports.buildRefreshURL();
}

Reports.prototype.buildRefreshURL = function(newReports, newStartDate, newEndDate) {
    if (!newReports) {
        newReports = this.reports;
    }

    // Make sure to respect the current ordering if there is no change in reports
    var currentReportsSet = [];
    $.each(this.reports, function(position, reportName) {
        currentReportsSet = currentReportsSet.concat(reportName);
    });
    var newReportsSet = [];
    $.each(newReports, function(position, reportName) {
        newReportsSet = newReportsSet.concat(reportName);
    });

    if ($(currentReportsSet).not(newReportsSet).length == 0 && $(newReportsSet).not(currentReportsSet).length == 0) {
        // Same set of reports
        newReports = this.reports;
    }

    var url = !this.loadFromFilePath ? this.buildBaseURL(this.basePath) : this.basePath;
    url += '?startDate=' + (newStartDate ? newStartDate : this.startDate);
    url += '&endDate=' + (newEndDate ? newEndDate : this.endDate);

    for (var position in newReports) {
        var joinKey = '&report' + position + '=';
        url += joinKey + newReports[position].join(joinKey);
        if (this.smoothingFunctions[position]) {
            url += '&smooth' + position + '=' + this.smoothingFunctions[position];
        }
    }

    return url;
};

Reports.prototype.buildDataURL = function(position, format) {
    var url = this.buildBaseURL('/plugins/killbill-analytics/reports');
    url += '?format=' + (format ? format : 'json')
    url += '&startDate=' + this.startDate;
    url += '&endDate=' + this.endDate;
    url += '&name=' + this.reports[position].join('&name=');
    if (this.smoothingFunctions[position]) {
        url += '&smooth=' + this.smoothingFunctions[position]
    }

    return url;
}

Reports.prototype.buildBaseURL = function(path) {
    return this.protocol + '://' + this.host + ':' + this.port + path;
}