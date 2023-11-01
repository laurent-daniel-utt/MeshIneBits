import * as THREE from 'three';
import { STLLoader } from 'STLLoader';
import {OrbitControls} from 'OrbitControls';
import {DragControls} from 'DragControls';



    //Scene
    const scene = new THREE.Scene();

    //Camera
    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 100000);
    camera.position.set(-80, 100, 200);

    //Renderer
    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.shadowMap.enabled = true;
    document.body.appendChild(renderer.domElement);

    //Lights
    let ambientLight = new THREE.AmbientLight(0xffffff, 0.3);
    let directionalLight = new THREE.DirectionalLight(0xffffff, 1);
    directionalLight.position.set(-30, 50, 150);
    scene.add(ambientLight);
    scene.add(directionalLight);

    // Grid
    const gridSize = 1000; // Size of the grid
    const gridDivisions = 10; // Number of grid divisions
    const gridColor = 0xffffff;
    const gridHelper = new THREE.GridHelper(gridSize, gridDivisions, gridColor);
    scene.add(gridHelper);


    // Set the background color
    const backgroundColor =0x7a7a7a;

    scene.background = new THREE.Color(backgroundColor);

    let arrowPos = new THREE.Vector3( 0,0,0 );
    scene.add( new THREE.ArrowHelper( new THREE.Vector3( 10,0,0 ), arrowPos, 60, 0x7F2020, 20, 10 ) );
    scene.add( new THREE.ArrowHelper( new THREE.Vector3( 0,10,0 ), arrowPos, 60, 0x207F20, 20, 10 ) );
    scene.add( new THREE.ArrowHelper( new THREE.Vector3( 0,0,10 ), arrowPos, 60, 0x20207F, 20, 10 ) );


// Create a loader for STL files
const loader = new STLLoader();

//Camera controls
const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;
controls.target.set(0, 55, 0);
const queryString = window.location.search;

const urlParams = new URLSearchParams(queryString);

const fileName = urlParams.get('name');

// Define the data to be sent as query parameters
const data = {
    x: 1.0,
    y: 2.0,
    z: 3.0,
    rot: 45.0
};


// Load the STL model
loader.load('/download-stl?name='+fileName, (geometry) => {
    // Create a mesh and add it to the scene
    const material = new THREE.MeshNormalMaterial();
    const mesh = new THREE.Mesh(geometry, material);
    mesh.isDraggable = true;
    scene.add(mesh)
    mesh.rotation.x = -Math.PI/2;


    // Set the camera position
    camera.position.z = 1000;

/*    //Plane
    const planeMaterial = new THREE.MeshBasicMaterial({color: 0X00ff00});
    const planeGeometry = new THREE.PlaneGeometry(10, 10);*/

    //Draggable objects
    const objectsToDrag = [mesh];
    const plane = new THREE.Mesh(planeGeometry, planeMaterial);

    const dragControls = new DragControls(objectsToDrag, camera, renderer.domElement);
    dragControls.activate()

    // Create an animation loop
    const animate = () => {
        requestAnimationFrame(animate);

        controls.update();

        renderer.render(scene, camera);
    };

    animate();
});


/*------------BACKEND------------*/

// Define the URL for the POST request
const url = '/post-pos';

// Define the request options
const requestOptions = {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(data).toString(),
};

// Send the POST request
fetch(url, requestOptions)
    .then(response => {
        if (response.status === 201) {
            return response.text();
        } else {
            throw new Error('Failed to create post');
        }
    })
    .then(responseText => {
        console.log('Server response: ' + responseText);
    })
    .catch(error => {
        console.error('Error: ' + error);
    });



