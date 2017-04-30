
var libraryJson = {};

function makeImageUrl(liteApp) {
  return 'https://chimbori.github.io/lite-apps/bin/library/112x112/' +
      encodeURIComponent(liteApp.name) + '.png';
}

function makeCreateUrl(liteApp) {
  var createUrl = 'https://hermit.chimbori.com/create?url=' + liteApp.url;
  if (liteApp.user_agent == 'desktop') {
    createUrl += '&ua=d';
  }
  if (liteApp.app != '') {
    createUrl += '&app=/lite-apps/' + encodeURIComponent(liteApp.app.replace(/ /g, '%20'));
  }
  return createUrl;
}

function LiteApp(props) {
  return <div className="lite-app-icon">
    <a href={ makeCreateUrl(props.liteApp) }>
      <img className="lite-app-icon-image" src={ makeImageUrl(props.liteApp) }/>
      <p>{ props.liteApp.name }</p>
    </a>
  </div>
}

function isDisplayed(liteApp) {
  return !liteApp.hasOwnProperty('display') || liteApp.display;
}

function Category(props) {
  return <div>
    <h3 className="lite-app-category-count">{props.category.apps.length} Lite Apps</h3>
    <h2>{ props.category.category }</h2>
    <div className="lite-app-category">
    {
      props.category.apps.map(function(liteApp, i) {
        if (!isDisplayed(liteApp)) {
          return null;
        }
        return <LiteApp liteApp={ liteApp } key={ i }/>;
      })
    }
    </div>
  </div>
}

function Library(props) {
  return <div> {
    props.library.map(function(category, i) {
      var isAtleastOneLiteAppDisplayed = false;
      for (var liteAppIndex in category.apps) {
        if (isDisplayed(category.apps[liteAppIndex])) {
          isAtleastOneLiteAppDisplayed = true;
          break;
        }
      }

      return isAtleastOneLiteAppDisplayed
          ? <Category category={ category } key={ i }/>
          : null;
    })
  } </div>
}


function updateDisplay(libraryJson) {
  ReactDOM.render(
    <Library library={ libraryJson }/>,
    document.querySelector('.lite-apps-json')
  );
}

function applyQueryFilter(queryText) {
  var isQueryBlank = queryText.length == 0;
  for (var categoryIndex in libraryJson) {
    var category = libraryJson[categoryIndex];
    for (var liteAppIndex in category.apps) {
      var liteApp = category.apps[liteAppIndex];
      liteApp.display = isQueryBlank;
      if (liteApp.name.toLowerCase().indexOf(queryText) != -1 ||
          liteApp.url.toLowerCase().indexOf(queryText) != -1) {
        liteApp.display = true;
      }
    }
  }
}

document.querySelector('#query').addEventListener('input', function(e) {
  applyQueryFilter(e.target.value.toLowerCase());
  updateDisplay(libraryJson);
});


function getQueryVariable(paramName) {
  var query = window.location.search.substring(1);
  var vars = query.split('&');
  for (var i = 0; i < vars.length; i++) {
    var pair = vars[i].split('=');
    if (pair[0] === paramName) {
      return pair[1];
    }
  }
  return '';
}

function fetchJson() {
  $.getJSON( "../bin/lite-apps/lite-apps.json", function(data) {
    libraryJson = data;
    applyQueryFilter(document.querySelector('#query').value.toLowerCase());
    updateDisplay(libraryJson);
  });
}

function showQueryParamInSearchBox() {
  var queryParam = getQueryVariable('q');
  if (queryParam != null && queryParam.length == 0) {
    try {
      var queryAsUrl = new URL(queryParam);
      if (queryAsUrl != null) {
        document.querySelector('#query').value = queryAsUrl.hostname;
      }
    } catch (e) {
      document.querySelector('#query').value = queryParam;
    }
  }
  document.querySelector('#query').focus();
}

showQueryParamInSearchBox();
fetchJson();
