/*
 * Copyright 2023 Haulmont.
 *
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
 */

import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export const externalStyles = css `
    .pvtUi { color: #333; }

    table.pvtTable {
        font-size: 8pt;
        text-align: left;
        border-collapse: collapse;
    }

    table.pvtTable thead tr th, table.pvtTable tbody tr th {
        background-color: #e6EEEE;
        border: 1px solid #CDCDCD;
        font-size: 8pt;
        padding: 5px;
    }

    table.pvtTable .pvtColLabel {text-align: center;}
    table.pvtTable .pvtTotalLabel {text-align: right;}

    table.pvtTable tbody tr td {
        color: #3D3D3D;
        padding: 5px;
        background-color: #FFF;
        border: 1px solid #CDCDCD;
        vertical-align: top;
        text-align: right;
    }

    .pvtTotal, .pvtGrandTotal { font-weight: bold; }

    .pvtVals { text-align: center; white-space: nowrap;}
    .pvtRowOrder, .pvtColOrder {
        cursor:pointer;
        width: 15px;
        margin-left: 5px;
        display: inline-block; }
    .pvtAggregator { margin-bottom: 5px ;}

    .pvtAxisContainer, .pvtVals {
        border: 1px solid gray;
        background: #EEE;
        padding: 5px;
        min-width: 20px;
        min-height: 20px;

        user-select: none;
        -webkit-user-select: none;
        -moz-user-select: none;
        -khtml-user-select: none;
        -ms-user-select: none;
    }
    .pvtAxisContainer li {
        padding: 8px 6px;
        list-style-type: none;
        cursor:move;
    }
    .pvtAxisContainer li.pvtPlaceholder {
        -webkit-border-radius: 5px;
        padding: 3px 15px;
        -moz-border-radius: 5px;
        border-radius: 5px;
        border: 1px dashed #aaa;
    }

    .pvtAxisContainer li span.pvtAttr {
        -webkit-text-size-adjust: 100%;
        background: #F3F3F3;
        border: 1px solid #DEDEDE;
        padding: 2px 5px;
        white-space:nowrap;
        -webkit-border-radius: 5px;
        -moz-border-radius: 5px;
        border-radius: 5px;
    }

    .pvtTriangle {
        cursor:pointer;
        color: grey;
    }

    .pvtHorizList li { display: inline; }
    .pvtVertList { vertical-align: top; }

    .pvtFilteredAttribute { font-style: italic }

    .pvtFilterBox{
        z-index: 100;
        width: 300px;
        border: 1px solid gray;
        background-color: #fff;
        position: absolute;
        text-align: center;
    }

    .pvtFilterBox h4{ margin: 15px; }
    .pvtFilterBox p { margin: 10px auto; }
    .pvtFilterBox label { font-weight: normal; }
    .pvtFilterBox input[type='checkbox'] { margin-right: 10px; margin-left: 10px; }
    .pvtFilterBox input[type='text'] { width: 230px; }
    .pvtFilterBox .count { color: gray; font-weight: normal; margin-left: 3px;}

    .pvtCheckContainer{
        text-align: left;
        font-size: 14px;
        white-space: nowrap;
        overflow-y: scroll;
        width: 100%;
        max-height: 250px;
        border-top: 1px solid lightgrey;
        border-bottom: 1px solid lightgrey;
    }

    .pvtCheckContainer p{ margin: 5px; }

    .pvtRendererArea { padding: 5px;}
`

// Register a module with ID for backwards compatibility.
registerStyles('', externalStyles, { moduleId: 'jmix-pivot-external-styles' });