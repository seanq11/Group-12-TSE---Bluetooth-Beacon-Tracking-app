//
//  AppDelegate.swift
//  MapTSE
//
//  Created by Sam on 06/05/2023.
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {



    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        //Allows for use of estimotes own methods, from there SDK.
        let locationBuilder = EILLocationBuilder()
        //assigns the name of the location on the cloud
        locationBuilder.setLocationName("room test 4")
        
        //Set shape and size of space/room
        //each coordinate marks a corner of the room to be built.
        //each unit is equivilent to a meter.
        locationBuilder.setLocationBoundaryPoints([
            EILPoint(x: 0.00, y: 0.00),
            EILPoint(x: 4.00, y: 0.00),
            EILPoint(x: 4.00, y: 6.00),
            EILPoint(x: 0.00, y: 6.00)])
        
        
        //beacon location
        //For each beacon, it assigns a wall(boundarySegmentIndex) based on the coordinates above.
        //the distance from the corner (inDistance) and which corner of the chosen wall(from:)
        locationBuilder.addBeacon(withIdentifier: "f813ca463315",
            atBoundarySegmentIndex: 0, inDistance: 2.0, from: .leftSide)
        locationBuilder.addBeacon(withIdentifier: "f6aab0aa67c9",
            atBoundarySegmentIndex: 1, inDistance: 3.0, from: .leftSide)
        locationBuilder.addBeacon(withIdentifier: "cc0fbab45152",
            atBoundarySegmentIndex: 3, inDistance: 3.0, from: .leftSide)
        
        //Compass Orientation (e.g. 0 = North, 90 = East,.etc.)
        //the direction of the room along the wall between the first two sets of coordinates.
        locationBuilder.setLocationOrientation(93)
        
        //Builds map and uploads it to estimote cloud account
        let location = locationBuilder.build()!
        
        //After adding a new app on estimote cloud, it assigns the id and token, as shown below.
        ESTConfig.setupAppID("maptse-fz9", andAppToken: "c42654855628b7d094873827cd4c045f")
        //sends a request to the estimote cloud to add the newly built location
        let addLocationRequest = EILRequestAddLocation(location: location)
        addLocationRequest.sendRequest { (location, error) in
            if error != nil {
                NSLog("Error when saving location: \(error)")
            } else {
                NSLog("Location saved successfully: \(location?.identifier)")
            }
        }
        
        // Override point for customization after application launch.
        return true
        
        
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
}

