var timeoutId = null;
var intervalId = null;
function cancelTimer() {
    if (timeoutId != null) {
        clearTimeout(timeoutId);
    }
    timeoutId = null;
}
function setTimer(invoke, timeout) {
    cancelTimer();
    timeoutId = setTimeout(invoke, timeout);
}
function cancelRepeater() {
    if (intervalId != null) {
        clearInterval(intervalId);
    }
    intervalId = null;
}
function setRepeater(invoke, interval) {
    cancelRepeater();
    intervalId = setInterval(invoke, interval);
}

var currentTreeId = null;
var shownGroupId = null;
var rows = 10;

/**
 * 初期化処理
 *
 */
function startup() {
    // 他サイトからのフレーム化を防止
    if (top != self) {
        top.location.href = locationhref;
    }

    // 最初の検索語入力エリアにフォーカスを移動
    var elems = document.getElementsByTagName("input");
    for (var i=0; i<elems.length; i++) {
        var elem = elems[i];
        if (elem.type == "text") {
            if (elem.id == "word" || elem.id.match(/^word\d+$/)) {
                elem.focus();
                break;
            }
        }
    }

    // 候補の初期化
    elems = document.getElementsByClassName("candidate");
    for (var i=0; i<elems.length; i++) {
        if (elems[i].tagName == "DIV") {
            initCandidate(elems[i]);
        }
    }
    if (elems.length > 0) {
        if (window.captureEvents) {
            // Mozilla
            window.captureEvents(Event.CLICK);
            window.onclick = canceled;
        } else {
            // IE
            document.onmouseup = canceled;
        }
    }
}
window.onload = startup;

/**
 * 候補を初期化します。
 *
 * @param elem 要素
 */
function initCandidate(elem) {
    var items = elem.getElementsByTagName("div");
    var selected = false;
    for (var i=0; i<items.length; i++) {
        var item = items[i];
        if (item.id == null) {
            continuel
        }
        if (item.id.match(/^word\d+_item\d+$/)) {
            item.onclick = itemClicked;
            item.onmouseover = itemOvered;
            if (!selected) {
                var opt = $(item.id.replace("_item", "_option"));
                if (opt.selected) {
                    // 選択さてれいるoptionを表示
                    var value = $(elem.id.replace("_tree", "_value"));
                    copyChildNodes(item, value);
                    selected = true;
                }
            }
        } else if (item.id.match(/^word\d+_groupItem\d+$/)) {
            item.onclick = itemClicked;
            item.onmouseover = itemOvered;
        } else if (item.id.match(/^word\d+_group\d+$/)) {
            // グループ内の各項目のサイズを最大のものに合せる
            // IEでは幅が自動調節されない、また、min-widthも有効にならない
            var groupItems = getChildItems(item);
            var w, h, y;
            var maxw = 80;
            var maxh = 0;
            for (var j=0; j<groupItems.length; j++) {
                // パッディングとボーダーの幅を除くサイズ
                w = Element.getWidth(groupItems[j]) - 10;
                maxw = Math.max(maxw, w);
                h = Element.getHeight(groupItems[j]) - 6;
                maxh = Math.max(maxh, h);
            }
            for (var j=0; j<groupItems.length; j++) {
                Element.setStyle(groupItems[j],
                                 {width:maxw+"px", height:maxh+"px"});
            }
            // グループ項目の表示領域の設定
            var scroll = $(item.id.replace("_group", "_scroll"));
            w = Element.getWidth(groupItems[0]);
            h = Element.getHeight(groupItems[0]) * rows;
            Element.setStyle(scroll, {width:w+"px", height:h+"px"});
            // 矢印の幅を設定
            var up = $(item.id.replace("_group", "_up"));
            var down = $(item.id.replace("_group", "_down"));
            w = groupItems[0].clientWidth;
            y = - (Element.getHeight(up));
            Element.setStyle(up, {top:y+"px", width:w+"px"});
            Element.setStyle(down, {top:h+"px", width:w+"px"});
        } else if (item.id.match(/^word\d+_up\d+$/)) {
            item.onmouseover = startScroll;
            item.onmouseout = stopScroll;
        } else if (item.id.match(/^word\d+_down\d+$/)) {
            item.onmouseover = startScroll;
            item.onmouseout = stopScroll;
        }
    }
    if (!selected) {
        // 未選択の場合は最初の項目を表示
        var item = $(elem.id.replace("_tree", "_item0"));
        var value = $(elem.id.replace("_tree", "_value"));
        copyChildNodes(item, value);
    }
}

/**
 * 子要素をコピーします。
 *
 * @param src コピー元
 * @param dest コピー先
 */
function copyChildNodes(src, dest) {
    // コピー先の子要素をすべて削除
    var nodes = dest.childNodes;
    for (var i=nodes.length-1; i>=0; i--) {
        dest.removeChild(nodes[i]);
    }
    // 複製をコピー先に追加
    nodes = src.childNodes;
    for (var i=0; i<nodes.length; i++) {
        dest.appendChild(nodes[i].cloneNode(true));
    }
}

/**
 * 指定されたIDを選択状態にします。
 *
 * @param selectedId 選択するoption要素のID
 */
function setSelected(selectedId) {
    var select = $(selectedId.replace(/_option\d+/, ""));
    var idx = selectedId.replace(/word\d+_option/, "");
    select.selectedIndex = idx;
}

/**
 * 指定されたグループの親グループを返します。
 *
 * @param group グループ
 * @return 親グループ
 */
function getParentGroup(group) {
    if (group.id.match(/^word\d+_tree$/)) {
        return null;
    }
    var item = $(group.id.replace("_group", "_groupItem"));
    return getGroup(item);
}

/**
 * 指定されたグループの項目を返します。
 *
 * @param group グループ
 * @return 項目
 */
function getChildItems(group) {
    return group.getElementsByTagName("div");
}

/**
 * 指定された項目のグループを返します。
 *
 * @param item 項目
 * @return グループ
 */
function getGroup(item) {
    var group = item.parentNode;
    while (group != null) {
        if (group.id.match(/^word\d+_group\d+$/)) {
            break;
        }
        if (group.id.match(/^word\d+_tree$/)) {
            break;
        }
        group = group.parentNode;
    }
    return group;
}

/**
 * 指定された候補グループを表示します。
 *
 * @param group 候補グループ
 */
function showGroup(group) {
    hideOtherGroup(group);
    var comp = $(group.id.replace("_group", "_component"));
    if (Element.getStyle(comp, "visibility") == "hidden") {
        expandGroup(group);
    }
}

/**
 * 指定された候補グループの表示状態を切り替えます。
 *
 * @param group 候補グループ
 */
function toggleGroup(group) {
    hideOtherGroup(group);
    var comp = $(group.id.replace("_group", "_component"));
    if (Element.getStyle(comp, "visibility") == "hidden") {
        expandGroup(group);
    } else {
        collapseGroup(group);
    }
}

/**
 * 指定された候補グループを展開します。
 *
 * @param group 候補グループ
 */
function expandGroup(group) {
    var groupItem = $(group.id.replace("_group", "_groupItem"));
    var comp = $(group.id.replace("_group", "_component"));
    var parentGroup = getParentGroup(group);
    var parentComp = $(parentGroup.id.replace("_group", "_component"));

    // コンポーネントの表示位置を設定
    var pos = Position.cumulativeOffset(parentGroup);
    var x = pos[0] + parentGroup.offsetWidth;
    var y = pos[1];
    var idx = group.id.replace(/word\d+_group/, "");
    if (idx > 0) {
        y += groupItem.offsetTop;
    }
    var zidx = parseInt(Element.getStyle(parentComp, "zIndex"));
    zidx = (zidx) ? zidx+1 : 100;
    Element.setStyle(comp, {zIndex:zidx, top:y+"px", left:x+"px"});

    // 先頭の項目を表示領域に表示
    Element.setStyle(group, {top:"0px"});

    var items = getChildItems(group);
    if (items.length > rows) {
        // 矢印が必要であれば表示
        var up = $(group.id.replace("_group", "_up"));
        Element.setStyle(up, {visibility:"visible"});
        var upImg = $(group.id.replace("_group", "_upImage"));
        upImg.src = "up_disable.gif";
        var down = $(group.id.replace("_group", "_down"));
        Element.setStyle(down, {visibility:"visible"});
        var downImg = $(group.id.replace("_group", "_downImage"));
        downImg.src = "down.gif";
    }

    // 表示
    Element.setStyle(comp, {visibility:"visible"});
    groupItem.addClassName("selected");
    shownGroupId = group.id;
}

/**
 * 指定された候補グループを収納します。
 *
 * @param group 候補グループ
 */
function collapseGroup(group) {
    var comp = $(group.id.replace("_group", "_component"));
    Element.setStyle(comp, {visibility:"hidden", zIndex:0});
    var up = $(group.id.replace("_group", "_up"));
    Element.setStyle(up, {visibility:"hidden"});
    var down = $(group.id.replace("_group", "_down"));
    Element.setStyle(down, {visibility:"hidden"});
    var item = $(group.id.replace("_group", "_groupItem"));
    item.removeClassName("selected");
    var parent = getParentGroup(group);
    if (parent == null || parent.id.match(/^word\d+_tree$/)) {
        shownGroupId = null;
        currentTreeId = null;
    } else {
        shownGroupId = parent.id;
    }
}

/**
 * 指定された候補グループ自身とその親候補グループ以外を非表示にします。
 *
 * @param group 候補グループ
 */
function hideOtherGroup(group) {
    while (shownGroupId != null) {
        if (shownGroupId == group.id) {
            break;
        }
        var parent = getParentGroup(group);
        while (parent != null) {
            if (parent.id == shownGroupId) {
                break;
            }
            parent = getParentGroup(parent);
        }
        if (parent != null) {
            break;
        }
        collapseGroup($(shownGroupId));
    }
}

/**
 * すべての候補グループを非表示にします。
 *
 */
function hideAllGroup() {
    while (shownGroupId != null) {
        collapseGroup($(shownGroupId));
    }
}

/**
 * onclickイベントハンドラ。
 *
 * @param evt イベント
 */
function itemClicked(evt) {
    cancelTimer();
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    var item = Event.element(evt);
    if (item.nodeType == 3 || item.tagName == "IMG") {
        // div要素内のテキストか外字イメージでのイベント
        item = item.parentNode;
    }

    if (item.id.match(/^word\d+_item\d+$/)) {
        // 選択
        setSelected(item.id.replace("_item", "_option"));
        var value = $(item.id.replace(/_item\d+/, "_value"));
        copyChildNodes(item, value);
        hideAllGroup();
    } else if (item.id.match(/^word\d+_groupItem\d+$/)) {
        // 下位階層の表示
        var group = $(item.id.replace("_groupItem", "_group"));
        var idx = group.id.replace(/word\d+_group/, "");
        if (idx > 0) {
            showGroup(group);
        } else {
            var treeId = group.id.replace(/_group\d+/, "_tree");
            if (currentTreeId != treeId) {
                hideAllGroup();
                currentTreeId = treeId;
            }
            toggleGroup(group);
        }
    }
    evt.cancelBubble = true;
}

/**
 * onmouseoverイベントハンドラ。
 *
 * @param evt イベント
 */
function itemOvered(evt) {
    if (currentTreeId == null) {
        return;
    }
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    var item = Event.element(evt);
    if (item.nodeType == 3 || item.tagName == "IMG") {
        // div要素内のテキストか外字イメージでのイベント
        item = item.parentNode;
    }

    // 子要素からの出入りは無視
    var from, to;
    if (evt.relatedTarget != null) {
        // Mozilla
        from = evt.relatedTarget;
        to = evt.target;
    } else if (evt.fromElement != null) {
        // IE
        from = evt.fromElement;
        to = evt.toElement;
    }
    if (to.id == item.id && Element.childOf(from, to)) {
        return;
    } else if (from.id == item.id && Element.childOf(to, from)) {
        return;
    }

    if (item.id.replace(/_\w*[iI]tem\d+/, "_tree") == currentTreeId) {
        // 展開中のツリーのみ処理
        setTimer("itemSelected('" + item.id + "')", 100);
        evt.cancelBubble = true;
    }
}
function itemSelected(itemId) {
    if (itemId.match(/^word\d+_item\d+$/)) {
        // 展開されている他候補グループを隠す
        var group = getGroup($(itemId));
        hideOtherGroup(group);
    } else if (itemId.match(/^word\d+_groupItem\d+$/)) {
        // 下位階層の表示
        var group = $(itemId.replace("_groupItem", "_group"));
        var idx = group.id.replace(/word\d+_group/, "");
        if (idx > 0) {
            showGroup(group);
        } else {
            var treeId = group.id.replace(/_group\d+/, "_tree");
            if (currentTreeId == treeId) {
                hideOtherGroup(group);
            }
        }
    }
}

/**
 * window.onclickイベントハンドラ
 *
 * @param evt イベント
 */
function canceled(evt) {
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    var item = Event.element(evt);
    if (item.nodeType == 3 || item.tagName == "IMG") {
        // テキストかイメージでのイベント
        item = item.parentNode;
    }
    if (item.id == null || !item.id.match(/^word\d+_\w*\d+$/)) {
        cancelTimer();
        hideAllGroup();
    }
}

/**
 * スクロール開始イベントハンドラ
 *
 * @param evt イベント
 */
function startScroll(evt) {
    if (intervalId != null) {
        return;
    }
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    var item = Event.element(evt);
    if (item.nodeType == 3 || item.tagName == "IMG") {
        // div要素内のテキストかイメージでのイベント
        item = item.parentNode;
    }

    // 子要素からの出入りは無視
    var from, to;
    if (evt.relatedTarget != null) {
        // Mozilla
        from = evt.relatedTarget;
        to = evt.target;
    } else if (evt.fromElement != null) {
        // IE
        from = evt.fromElement;
        to = evt.toElement;
    }
    if (to.id == item.id && Element.childOf(from, to)) {
        return;
    } else if (from.id == item.id && Element.childOf(to, from)) {
        return;
    }

    if (item.id.match(/^word\d+_up\d+$/)) {
        setRepeater("scrollUp('" + item.id + "')", 100);
    } else if (item.id.match(/^word\d+_down\d+$/)) {
        setRepeater("scrollDown('" + item.id + "')", 100);
    }
    evt.cancelBubble = true;
}

/**
 * スクロール停止イベントハンドラ
 *
 * @param evt イベント
 */
function stopScroll(evt) {
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    var item = Event.element(evt);
    if (item.nodeType == 3 || item.tagName == "IMG") {
        // div要素内のテキストかイメージでのイベント
        item = item.parentNode;
    }

    // 子要素からの出入りは無視
    var from, to;
    if (evt.relatedTarget != null) {
        // Mozilla
        from = evt.target;
        to = evt.relatedTarget;
    } else if (evt.fromElement != null) {
        // IE
        from = evt.fromElement;
        to = evt.toElement;
    }
    if (to.id == item.id && Element.childOf(from, to)) {
        return;
    } else if (from.id == item.id && Element.childOf(to, from)) {
        return;
    }

    cancelRepeater();
    evt.cancelBubble = true;
}

/**
 * 上スクロールイベントハンドラ
 *
 * @param itemId 項目ID
 */
function scrollUp(itemId) {
    var group = $(itemId.replace("_up", "_group"));
    var scroll = $(itemId.replace("_up", "_scroll"));
    var items = getChildItems(group);
    var y = parseInt(Element.getStyle(group, "top"));
    var h = parseInt(Element.getStyle(scroll, "height"));
    var dh = Element.getHeight(items[0]);
    if (y+dh <= 0) {
        hideOtherGroup(group);
        Element.setStyle(group, {top:(y+dh)+"px"});
        var downImg = $(itemId.replace("_up", "_downImage"));
        downImg.src = "down.gif";
    } else {
        cancelRepeater();
    }
    if (y+dh >=0) {
        var upImg = $(itemId.replace("_up", "_upImage"));
        upImg.src = "up_disable.gif";
    }
}

/**
 * 下スクロールイベントハンドラ
 *
 * @param itemId 項目ID
 */
function scrollDown(itemId) {
    var group = $(itemId.replace("_down", "_group"));
    var scroll = $(itemId.replace("_down", "_scroll"));
    var items = getChildItems(group);
    var y = parseInt(Element.getStyle(group, "top"));
    var h = parseInt(Element.getStyle(scroll, "height"));
    var dh = Element.getHeight(items[0]);
    var max = h - (items.length * dh);
    if (y-dh >= max) {
        hideOtherGroup(group);
        Element.setStyle(group, {top:(y-dh)+"px"});
        var upImg = $(itemId.replace("_down", "_upImage"));
        upImg.src = "up.gif";
    } else {
        cancelRepeater();
    }
    if (y-dh <= max) {
        var downImg = $(itemId.replace("_down", "_downImage"));
        downImg.src = "down_disable.gif";
    }
}

// end of webbook.js
