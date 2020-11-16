import AppInvokeSDK

@objc(AllInOneSDK)
class AllInOneSDK: CDVPlugin {
    var callbackId: String?
    var appInvoke: AppInvokeSDK.AIHandler!

    public override func pluginInitialize() {
        appInvoke = AppInvokeSDK.AIHandler()
    }

    public override func handleOpenURL(_ notification: Notification!) {
        if let url = notification.object as? URL {
            handleURL(url: url)
        }
    }

    public override func handleOpenURL(withApplicationSourceAndAnnotation notification: Notification!) {
        if let dictionary = notification.object as? [AnyHashable: Any],
            let url = dictionary["url"] as? URL {
            handleURL(url: url)
        }
    }

    @objc(startTransaction:)
    func startTransaction(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        guard let requestDict: [String: Any] = ((command.arguments)?.first) as? [String: Any] else {
            returnWithError(msg: "Data sent is not correct")
            return
        }
        guard let orderId: String = requestDict["orderId"] as? String, !orderId.isEmpty else {
            returnWithError(msg: "order ID is not available")
            return
        }
        guard let txnToken: String = requestDict["txnToken"] as? String, !txnToken.isEmpty else {
            returnWithError(msg: "transaction Token is not available")
            return
        }
        guard let merchantId: String = requestDict["mid"] as? String, !merchantId.isEmpty else {
            returnWithError(msg: "merchant ID is not available")
            return
        }
        guard let amount: String = requestDict["amount"] as? String, !amount.isEmpty else {
            returnWithError(msg: "amount is not available")
            return
        }
        guard let callbackUrl: String = requestDict["callbackUrl"] as? String, !callbackUrl.isEmpty else {
            returnWithError(msg: "callbackUrl is not available")
            return
        }
        var isStaging: AIEnvironment = .production
        if let stage = requestDict["isStaging"] as? Int {
            if stage != 0 {
                isStaging = .staging
            }
        }
        var restrictAppInvoke: Bool = false
        if let restriction = requestDict["restrictAppInvoke"] as? Int {
            if restriction != 0 {
                restrictAppInvoke = true
            }
        }
        self.appInvoke.setBridgeName(name: "cordova")
        self.appInvoke.restrictAppInvokeFlow(restrict: restrictAppInvoke)
        self.commandDelegate.run {
            self.appInvoke.openPaytm(merchantId: merchantId, orderId: orderId, txnToken: txnToken, amount: amount, callbackUrl: callbackUrl, delegate: self, environment: isStaging)
        }
    }
}

extension AllInOneSDK {
    private func returnWithError(msg: String) {
        returnWithResponse(isSuccess: false, message: msg, response: "")
    }

    private func returnWithResponse(isSuccess: Bool, message: String, response: String) {
        var data: [String: AnyObject] = [:]
        data["response"] = response as AnyObject
        data["message"] = message as AnyObject
        if isSuccess {
            let pluginResult = CDVPluginResult.init(status: .ok, messageAs: data)
            self.commandDelegate!.send(pluginResult, callbackId: callbackId);
        } else {
            let pluginResult = CDVPluginResult.init(status: .error, messageAs: message)
            self.commandDelegate!.send(pluginResult, callbackId: callbackId);
        }
    }

    private func handleURL(url: URL) {
        let dict = self.separateDeeplinkParamsIn(url: url.absoluteString, byRemovingParams: nil)
        let jsonTxt = getStringFromDictionary(dictionary: dict)
        returnWithResponse(isSuccess: true, message: "", response: jsonTxt ?? "")
    }

    private func separateDeeplinkParamsIn(url: String?, byRemovingParams rparams: [String]?)  -> [String: String] {
        guard let url = url else {
            return [String : String]()
        }

        var urlString = stringByRemovingDeeplinkSymbolsIn(url: url)

        var paramList = [String : String]()
        let pList = urlString.components(separatedBy: CharacterSet.init(charactersIn: "&?"))
        for keyvaluePair in pList {
            let info = keyvaluePair.components(separatedBy: CharacterSet.init(charactersIn: "="))
            if let fst = info.first , let lst = info.last, info.count == 2 {
                paramList[fst] = lst.removingPercentEncoding
                if let rparams = rparams, rparams.contains(info.first!) {
                    urlString = urlString.replacingOccurrences(of: keyvaluePair + "&", with: "")
                    //Please dont interchage the order
                    urlString = urlString.replacingOccurrences(of: keyvaluePair, with: "")
                }
            }
            if info.first == "response" {
                paramList["response"] = keyvaluePair.replacingOccurrences(of: "response=", with: "").removingPercentEncoding
            }
        }
        if let trimmedURL = pList.first {
            paramList["trimmedurl"] = trimmedURL
        }
        return paramList
    }

    private func stringByRemovingDeeplinkSymbolsIn(url: String) -> String {
        var urlString = url.replacingOccurrences(of: "$", with: "&")

        if let range = urlString.range(of: "&"), urlString.contains("?") == false{
            urlString = urlString.replacingCharacters(in: range, with: "?")
        }
        return urlString
    }

    private func getStringFromDictionary(dictionary: [String: Any]) -> String? {
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dictionary,options: .prettyPrinted) else {
            return nil
        }
        return (String(data: jsonData, encoding: .ascii) ?? "")
    }
}

extension AllInOneSDK: AIDelegate {
    func openPaymentWebVC(_ controller: UIViewController?) {
        if let webController = controller {
            self.viewController.present(webController, animated: true, completion: nil)
        } else {
            returnWithResponse(isSuccess: false, message: "Error loading web page", response: "")
        }
    }

    func didFinish(with status: AIPaymentStatus, response: [String : Any]) {
        var responseDict: [String: Any] = response
        var state = "fail"
        if status == .success {
            state = "success"
        } else if status == .pending {
            state = "pending"
        }
        responseDict["result"] = state
        let jsonTxt = getStringFromDictionary(dictionary: responseDict)
        returnWithResponse(isSuccess: true, message: state, response: jsonTxt ?? "")
    }
}
