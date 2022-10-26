var exec = require('cordova/exec');

//Pagamento
exports.getTitulo = function (success, error) {
    exec(success, error, 'MainActivity', 'getTitulo');
};
exports.pagamento = function (params, success, error) {
    exec(success, error, 'MainActivity', 'pagamento', [params]);
};
exports.GetStringImpressao = function (success, error) {
    exec(success, error, 'MainActivity', 'GetStringImpressao');
};
exports.limpaTransacao = function (success, error) {
    exec(success, error, 'MainActivity', 'limpaTransacao');
};
exports.cancelarTransacao = function (success, error) {
    exec(success, error, 'MainActivity', 'cancelarTransacao');
};
exports.GetDadosTransacao = function (success, error) {
    exec(success, error, 'MainActivity', 'GetDadosTransacao');
};


//Metodos Impressora
exports.checarImpressora = function (success, error) {
    exec(success, error, 'MainActivity', 'checarImpressora');
};
exports.imprimir = function (params, success, error) {
    exec(success, error, 'MainActivity', 'imprimir', [params]);
};
exports.impressoraOutput = function (params, success, error) {
    exec(success, error, 'MainActivity', 'impressoraOutput', [params]);
};

//Metodo Beep
exports.beep = function (success, error) {
    exec(success, error, 'MainActivity', 'beep');
};

//Metodos Led
exports.ledOn = function (success, error) {
    exec(success, error, 'MainActivity', 'ledOn');
};
exports.ledOff = function (success, error) {
    exec(success, error, 'MainActivity', 'ledOff');
};
exports.ledRedOn = function (success, error) {
    exec(success, error, 'MainActivity', 'ledRedOn');
};
exports.ledBlueOn = function (success, error) {
    exec(success, error, 'MainActivity', 'ledBlueOn');
};
exports.ledGreenOn = function (success, error) {
    exec(success, error, 'MainActivity', 'ledGreenOn');
};
exports.ledOrangeOn = function (success, error) {
    exec(success, error, 'MainActivity', 'ledOrangeOn');
};
exports.ledRedOff = function (success, error) {
    exec(success, error, 'MainActivity', 'ledRedOff');
};
exports.ledBlueOff = function (success, error) {
    exec(success, error, 'MainActivity', 'ledBlueOff');
};
exports.ledGreenOff = function (success, error) {
    exec(success, error, 'MainActivity', 'ledGreenOff');
};
exports.ledOrangeOff = function (success, error) {
    exec(success, error, 'MainActivity', 'ledOrangeOff');
};
