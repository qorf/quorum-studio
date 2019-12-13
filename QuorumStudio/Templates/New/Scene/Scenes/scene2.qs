{
    "Camera" : {
        "Name" : "Main Camera",
        "Description" : "This is the primary camera used in the game",
        "Location" : [0,0,0],
        "Direction" : [0,0,1],
        "Up" : [0,1,0]
    },
    "Physics3D" : {
        "Enabled" : true,
        "Gravity" : [0.0, -9.8, 0.0]
    },
    "Models": {
        "1" : {
            "Type" : "Cube",
            "Location" : [-3,0,5],
            "Size" : [2,2,2],
            "Color" : [1.0, 0, 0, 1],
            "Name" : "Cubey McCubeFace",
            "Description" : "This is the major cube of cubeyness",
            "Physics" : true,
            "Responsiveness" : "Responsive"
        },
        
        "2" : {
            "Type" : "Sphere",
            "Location" : [3,0,5],
            "Size" : [3,3,3],
            "Color" : [0, 1.0, 0, 1],
            "Name" : "The Sphere",
            "Description" : "My sphere"
        }
    },
    "Lights" : {
        "1" : {
            "Type" : "Ambient",
            "Color" : [0.2, 0.2, 0.2, 1]
        },
        "2" : {
            "Type" : "Directional",
            "Direction" : [1,-4,2],
            "Color" : [1, 1, 1, 1]
        }
    }
}