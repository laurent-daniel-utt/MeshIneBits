import * as THREE from 'three';
import { STLLoader } from 'STLLoader';
import { TransformControls } from 'TransformControls'
import { OrbitControls } from 'OrbitControls'

//Scene
const scene = new THREE.Scene();

//Camera
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 100000);
camera.position.set(-80, 100, 200);

const material = new THREE.MeshNormalMaterial();

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

const orbitControls = new OrbitControls(camera, renderer.domElement);
const transformControls = new TransformControls(camera, renderer.domElement)
transformControls.setMode('translate')
scene.add(transformControls)

const queryString = window.location.search;
console.log(queryString);
const urlParams = new URLSearchParams(queryString);

const fileName = urlParams.get('name');
var mesh = null;
// Load the STL model
loader.load('model-viewer/download-stl?name='+fileName, (geometry) => {
    // Create a mesh and add it to the scene

    mesh = new THREE.Mesh(geometry, material);

    scene.add(mesh)
    transformControls.attach(mesh)
});

transformControls.addEventListener('dragging-changed', function (event) {
    orbitControls.enabled = !event.value
})
// Replace 'your-url' with the actual URL from which you want to fetch JSON data
const apiUrl = '/bit2d';

// Make a fetch request to the API
fetch(apiUrl)
    .then(response => {
        // Check if the request was successful (status code 200)
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        // Parse the response as JSON
        return response.json();
    })
    .then(data => {
        // Print the JSON data to the console
        console.log(data);
        data.forEach(layer => {
            layer.bits.forEach(bit => {
                const points = [];

                bit.bitSidesSegments.forEach(bitSide => {
                    points.push(new THREE.Vector3(bitSide.start.x, bitSide.start.y, layer.z));
                    //vertices.push(vertex.x, vertex.y, vertex.z);
                });

                points.push(points[0]);

                let geometry = new THREE.BufferGeometry().setFromPoints(points)
                let line = new THREE.Line(
                    geometry,
                    new THREE.LineBasicMaterial({color: 1})
                )
                scene.add(line)



            });
        });
    })
    .catch(error => {
        // Handle any errors that occurred during the fetch
        console.error('Fetch error:', error);
    });

transformControls.addEventListener('dragging-changed', function (event) {
    orbitControls.enabled = !event.value;
});

window.addEventListener('mouseup', onMouseUp);


window.addEventListener('keydown', function (event) {
    switch (event.key) {
        case 'g':
            transformControls.setMode('translate')
            break
        case 'r':
            transformControls.setMode('rotate')
            break
        case 's':
            transformControls.setMode('scale')
            break
    }
})

window.addEventListener('resize', onWindowResize, false)
function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight
    camera.updateProjectionMatrix()
    renderer.setSize(window.innerWidth, window.innerHeight)
    render()
}


function animate() {
    requestAnimationFrame(animate)

    render()


}

function render() {
    renderer.render(scene, camera)
}

function onMouseUp(event) {
    console.log("onMouseUp");
    // Define the data to be sent as query parameters
    const data = {
        x: mesh.position.x,
        y: mesh.position.y,
        z: mesh.position.z,
        rot: 0
    };

    const url = 'model-viewer/post-pos';

    const requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(data).toString(),
    };

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
}

animate()
