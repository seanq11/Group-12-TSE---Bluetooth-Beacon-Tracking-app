//
//  ViewController.swift
//  MapTSE
//
//  Created by Sam on 06/05/2023.
//

import UIKit

// all code below from the tutorial to update location on a map. Tutorial from Estimote website.
// Used to indentify if there is an issue with the connection to estimote cloud or it its something else
// Connects to estimote cloud and checks if it can find the requested location


// 1. Add the EILIndoorLocationManagerDelegate protocol
class ViewController: UIViewController, EILIndoorLocationManagerDelegate  {
    
    // 2. Add the location manager
    let locationManager = EILIndoorLocationManager()
    var location: EILLocation!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // 3. Set the location manager's delegate
        self.locationManager.delegate = self
        
        //Connects App to estimote cloud
        ESTConfig.setupAppID("maptse-fz9", andAppToken: "c42654855628b7d094873827cd4c045f")
        
        //Fetches the location from estimote cloud
        let fetchLocationRequest = EILRequestFetchLocation(locationIdentifier: "lab-room-1301")
        fetchLocationRequest.sendRequest { (location, error) in
            if location != nil {
                self.location = location!
                self.locationManager.startPositionUpdates(for: self.location)
            } else {
                // states if the location can be found on the cloud.
                print("can't fetch location: \(error)")
            }
        }
        
        
    }
}
// ...
