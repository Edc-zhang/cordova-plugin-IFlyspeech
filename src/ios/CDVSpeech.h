//
//  CDVSpeech.h
//  ZJxunfeiDemo-OC
//
//  Created by Edc.zhang on 2017/2/13.
//  Copyright © 2017年 Edc.zhang. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <Cordova/CDV.h>

//#import "iflyMSC/iflyMSC.h"
#import "iflyMSC.framework/Headers/IFlyMSC.h"


@interface CDVSpeech : CDVPlugin <IFlySpeechRecognizerDelegate,IFlySpeechSynthesizerDelegate,IFlyRecognizerViewDelegate>{
    
}
@property (nonatomic, copy) NSString* appId;
@property (nonatomic, strong) NSString* callbackId;
@property (nonatomic, strong) IFlySpeechRecognizer* recognizer;
@property (nonatomic, strong) IFlySpeechSynthesizer* synthesizer;
@property(nonatomic,strong) IFlyRecognizerView      *iflyRecognizerView;


- (void)startListening:(CDVInvokedUrlCommand*)command;
- (void)stopListening:(CDVInvokedUrlCommand*)command;
- (void)cancelListening:(CDVInvokedUrlCommand*)command;

- (void)startSpeaking:(CDVInvokedUrlCommand*)command;
- (void)pauseSpeaking:(CDVInvokedUrlCommand*)command;
- (void)resumeSpeaking:(CDVInvokedUrlCommand*)command;
- (void)stopSpeaking:(CDVInvokedUrlCommand*)command;

@end
