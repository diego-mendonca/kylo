/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/profile-history/profile-invalid-results.html',
            controller: "FeedProfileInvalidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    };

    var controller =  function($scope,$http,$stateParams, FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.data = [];
        this.loadingFilterOptions = false;
        this.loadingData = false;
        this.limitOptions = [10, 50, 100, 500, 1000];
        this.limit = this.limitOptions[2];

        this.filterOptions = [
            {name: 'None', objectShortClassType: ''},
            {name: 'Type Conversion', objectShortClassType: 'Not convertible to'}
        ];
        this.filter = self.filterOptions[0];

        //noinspection JSUnusedGlobalSymbols
        this.onLimitChange = function() {
            getProfileValidation();
        };

        //noinspection JSUnusedGlobalSymbols
        this.onFilterChange = function() {
            getProfileValidation();
        };

        $scope.gridOptions = {
            columnDefs: [],
            rowHeight: 70,
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true,
            useExternalSorting: false
        };

        var transformFn = function(row,columns,displayColumns){
            var invalidFields = [];
            var invalidFieldMap = {};
            row.invalidFields = invalidFields;
            row.invalidFieldMap = invalidFieldMap;
            row.invalidField = function(column){
                return this.invalidFieldMap[column];
            };
            var _index = _.indexOf(displayColumns,'dlp_reject_reason');
            var rejectReasons = row[columns[_index]];
            if(rejectReasons != null){
                rejectReasons = angular.fromJson(rejectReasons);
            }
            if(rejectReasons != null){
                angular.forEach(rejectReasons,function(rejectReason){
                    if(rejectReason.scope =='field'){
                        var field = rejectReason.field;
                        var copy = angular.copy(rejectReason);
                        _index = _.indexOf(displayColumns,field);
                        copy.fieldValue = row[columns[_index]];
                        invalidFields.push(copy);
                        invalidFieldMap[columns[_index]] = copy;
                    }
                });
            }

        };

        var addCellTemplate = function(columns) {
            var cellTemplate =
                '<div layout="column" class="ui-grid-cell-contents">' +
                '        <div flex="100" ng-class="{ \'warn\': row.entity.invalidField(col.colDef.name) != undefined }">' +
                '            {{row.entity[col.colDef.name]}}' +
                '        </div>' +
                '        <div flex="100" class="violation hint">' +
                '                {{row.entity.invalidField(col.colDef.name).rule}}' +
                '                <br>' +
                '                {{row.entity.invalidField(col.colDef.name).reason}}' +
                '        </div>' +
                '</div>';

            angular.forEach(columns, function(column) {
                column.cellTemplate = cellTemplate;
            });

            return columns;
        };

        var errorFn = function (err) {
            self.loadingData = false;
        };
        function getProfileValidation(){
            self.loadingData = true;

            var successFn = function (response) {
                var result = self.queryResults = HiveService.transformResultsToUiGridModel(response, [], transformFn);
                $scope.gridOptions.columnDefs = addCellTemplate(result.columns);
                $scope.gridOptions.columnDefs = _.reject($scope.gridOptions.columnDefs, function(col) {
                    return col.name == 'dlp_reject_reason'
                });
                $scope.gridOptions.data = result.rows;

                self.loadingData = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','invalid');
            };

            var promise = $http.get(
                RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(self.model.id),
                { params:
                    {
                        'processingdttm': self.processingdttm,
                        'limit': self.limit,
                        'filter': _.isUndefined(self.filter) ? '' : self.filter.objectShortClassType
                    }
                });
            promise.then(successFn, errorFn);
            return promise;
        }

        function getFilterOptions() {
            self.loadingFilterOptions = true;
            var filterOptionsOk = function(response) {
                self.filterOptions = _.union(self.filterOptions, response.data);
                self.loadingFilterOptions = false;
            };
            var promise = $http.get(RestUrlService.AVAILABLE_VALIDATION_POLICIES, {cache:true});
            promise.then(filterOptionsOk, errorFn);
            return promise;
        }

        getFilterOptions();
        getProfileValidation();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileInvalidResultsController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileInvalid', directive);

})();
