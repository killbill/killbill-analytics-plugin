function ReportsUrls(reportName) {
    this.url = reportName;

    // See com.ning.billing.osgi.bundles.analytics.reports.ReportSpecification
    this.reportSpecificationsSeparator = '^';
    this.reportSpecificationSeparator = ':';

    // See com.ning.billing.osgi.bundles.analytics.reports.sql.Cases
    this.groupsSeparator = '|';
}

ReportsUrls.prototype.addDimension = function(dimension, groups) {
    var dimensionWithGroups = dimension;

    var that = this;
    $.each(groups || [], function(i, group) {
        if (i == 0) {
            dimensionWithGroups += '(';
        }

        dimensionWithGroups += groups[i];

        if (i == groups.length - 1) {
            dimensionWithGroups += ')';
        } else {
            dimensionWithGroups += that.groupsSeparator;
        }
    });

    return this.addSpecification('dimension', dimensionWithGroups);
}

ReportsUrls.prototype.addMetric = function(metric) {
    return this.addSpecification('metric', metric);
}

ReportsUrls.prototype.addFilter = function(filter) {
    return this.addSpecification('filter', filter);
}

ReportsUrls.prototype.addSpecification = function(specificationString, specificationValue) {
    this.url += this.reportSpecificationsSeparator + specificationString + this.reportSpecificationSeparator + encodeURIComponent(specificationValue);
    return this;
}
