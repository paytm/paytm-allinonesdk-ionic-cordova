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
        self.commandDelegate.run {
            self.appInvoke.openPaytm(merchantId: merchantId, orderId: orderId, txnToken: txnToken, amount: amount, callbackUrl: callbackUrl, delegate: self, environment: isStaging)
        }
    }
}

extension AllInOneSDK {
    private func returnWithError(msg: String) {
        returnWithResponse(message: msg, response: "")
    }

    private func returnWithResponse(message: String, response: String) {
        var data: [String: AnyObject] = [:]
        data["response"] = response as AnyObject
        data["message"] = message as AnyObject
        let pluginResult = CDVPluginResult.init(status: .ok, messageAs: data)
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);
    }

    private func handleURL(url: URL) {
        let urlString: String = url.absoluteString
        returnWithResponse(message: "", response: urlString)
    }
}

extension AllInOneSDK: AIDelegate {
    func openPaymentWebVC(_ controller: UIViewController?) {
        if let webController = controller {
            self.viewController.present(webController, animated: true, completion: nil)
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
        returnWithResponse(message: state, response: responseDict.description)
    }
}
