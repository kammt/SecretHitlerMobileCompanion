let prevGameData = null;

const getGameJSON = () => {
    const Http = new XMLHttpRequest();
    const url=`http://${location.host}/getGameJSON` ;
    Http.open("GET", url);
    Http.send();
    
    Http.onreadystatechange = (e) => {
      console.log(Http.responseText);
      return Http.responseText;
    }
};

const loadPage = () => {
    // Make an HTTP request to the server to receive the current game state
    //const gameData = getGameJSON();

    gameData = JSON.parse('{"game":{"id":"758439574802","players":["Mario","Tobi","David","Felix","Eva","Niclas","Leander"],"plays":[{"type":"legislative-session","num":1,"president":"David","chancellor":"Tobi","rejected":false,"president_claim":"RRB","chancellor_claim":"RB","policy_played":"B","veto":false},{"type":"legislative-session","num":2,"president":"Felix","chancellor":"David","rejected":false,"president_claim":"RBB","chancellor_claim":"RR","policy_played":"R","veto":false},{"type":"legislative-session","num":3,"president":"Felix","chancellor":"David","rejected":true},{"type":"legislative-session","num":4,"president":"Felix","chancellor":"David","rejected":false,"president_claim":"RBB","chancellor_claim":"RR","policy_played":"R","veto":true},{"type":"executive-action","executive_action_type":"execution","president":"Felix","target":"David"},{"type":"executive-action","executive_action_type":"investigate-loyalty","president":"Felix","target":"David","claim":"B"},{"type":"executive-action","executive_action_type":"policy-peek","president":"Felix","claim":"RRR"},{"type":"executive-action","executive_action_type":"special-election","president":"Felix","target":"David"},{"type":"shuffle","fas-policies":11,"lib-policies":6}]}}');

    let prevPlays = prevGameData == null ? null : prevGameData.game.plays;
    let plays = gameData.game.plays;

    console.log(prevPlays);
    console.log(plays);

    let gameLogSection = $('#game-log'); 
    let gameLogSectionInnerHTML = gameLogSection.html();

    console.log(prevPlays != null ? prevPlays.length - 1 : 0);
    
    for(let i = prevPlays != null ? prevPlays.length - 1 : 0; i < plays.length; i++) {
        let play = plays[i];
        console.log(play.type);
        if(play.type === "legislative-session") {
            gameLogSectionInnerHTML += getLegislativeSessionHTML(play)
        } else if(play.type === "executive-action") {
            gameLogSectionInnerHTML += getExecutiveActionHTML(play);
        }
    }

    gameLogSection.html(gameLogSectionInnerHTML);

    prevGameData = gameData;
};

const getLegislativeSessionHTML = (play) => {
    let result = `<div class="game-action legislative-session"><h1 class="game-action-title">Legislative Session #${play.num} ${play.rejected === true ? '- Rejected' : ''} ${play.veto === true ? '- Vetoed' : ''}</h1><div class="president">
        <img class="token president-token" src="images/president-token.png" alt="president-token"><span class="president-name">${play.president}</span>
        <span class="right-align">${play.rejected === false ? getColouredClaim(play.president_claim) : ''}</span></div>
        <div class="chancellor"><img class="token chancellor-token" src="images/chancellor-token.png" alt="chancellor-token">
        <span class="chancellor-name ${play.rejected === true ? 'cross-out' : ''}">${play.chancellor}</span>
        <span class="right-align">${play.rejected === false ? getColouredClaim(play.chancellor_claim) : ''}</span></div>`

    if(play.rejected === false) {
        result += `<div class="policy-played"><span class="policy-played-span ${play.veto === true ? 'cross-out' : ''}">Policy played</span><img class="policy-icon" src="images/${play.policy_played === 'B' ? 'bird' : 'skull'}.png" alt="policy-played"></div>`
    }

    result += '</div>';
    return result;
};

const getExecutiveActionHTML = (play) => {
    let executiveActionText = "";

    let presidentHTML = getPlayerHTML(play.president);
    let targetHTML = play.target != undefined ? getPlayerHTML(play.target) : ''; 
    switch(play.executive_action_type) {
        case 'execution':
            executiveActionText = `President ${presidentHTML} selects to execute ${targetHTML}.`;
            break;
        case 'investigate-loyalty':
            executiveActionText = `President ${presidentHTML} sees the party membership of ${targetHTML} and claims to see a member of the ${play.claim === 'B' ? 'liberal' : 'fascist'} team.`
            break;
        case 'policy-peek':
            executiveActionText = `President ${presidentHTML} peeks at the next three policies and claims to see ${getColouredClaim(play.claim)}.`;
            break;
        case 'special-election':
            executiveActionText = `President ${presidentHTML} has chosen to special elect ${targetHTML} as president.`;
            break;
        default:
            executiveActionText = 'Unknwon executive action type';
            break;
    }

    return `<div class="game-action executive-action"><div class="executive-action-top"><h1 class="game-action-title executive-action-title">Executive Action</h1>
        <img class="executive-action-icon" src="images/${play.executive_action_type}.png" alt="${play.executive_action_type}-icon"></div>
        <p class="executive-action-text">${executiveActionText}</p> </div>`
};

const getPlayerHTML = (name) => {
    return `<span class="player-name">${name != null ? name : ''}</span>`
};

const getColouredClaim = (claim) => {
    console.log(claim.split(''));
    let result = '';
    let chars = claim.split('');

    chars.forEach((char) => {
        result += `<span class="${char === 'R' ? 'claim-span-red' : 'claim-span-blue'}">${char}</span>`
    });

    return result;
};

loadPage();

alert("jgkdfl");

