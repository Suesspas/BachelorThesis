/*
Using artifical neural network and genetic algorithm to train bot to play Flappy Bird
Copyright (C) 2020 Dušan Erdeljan

This file is part of neuroevolution-flappy-bird

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package jump.neuralNetwork;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NeuralNetwork {
	
	public class FlattenNetwork {
		public List<Integer> neurons;
		public List<Float> weights;
		
		public FlattenNetwork() {
			this.neurons = new LinkedList<Integer>();
			this.weights = new LinkedList<Float>();
		}
	}

	public List<Layer> layers;
	
	private NeuralNetwork() {
		this.layers = new ArrayList<Layer>();
	}
	
	public NeuralNetwork(int... topology) {
		this();
		int prevInputs = 0;
		for (int i = 0; i < topology.length; i++) {
			this.layers.add(new Layer(topology[i], prevInputs));
			prevInputs = topology[i];
		}
	}
	
	public FlattenNetwork flatten() {
		FlattenNetwork net = new FlattenNetwork();
		for (Layer layer: this.layers) {
			net.neurons.add(layer.neurons.size());
			for (Neuron neuron: layer.neurons) {
				net.weights.addAll(neuron.weights);
			}
		}
		return net;
	}
	
	public static NeuralNetwork expand(FlattenNetwork net) {
		NeuralNetwork nn = new NeuralNetwork();
		int prevInput = 0;
		int weightIndex = 0;
		for (int neuronCount: net.neurons) {
			Layer layer = new Layer(neuronCount, prevInput);
			for (int i = 0; i < layer.neurons.size(); i++) {
				for (int j = 0; j < layer.neurons.get(i).weights.size(); j++) {
					layer.neurons.get(i).weights.set(j, net.weights.get(weightIndex++));
				}
			}
			prevInput = neuronCount;
			nn.layers.add(layer);
		}
		return nn;
	}
	
	public float[] eval(float... inputs) {
		for (int i = 0; i < inputs.length; i++) {
			this.layers.get(0).neurons.get(i).value = inputs[i];
		}
		Layer prevLayer = this.layers.get(0);
		for (int i = 1; i < this.layers.size(); i++) {
			this.layers.get(i).eval(prevLayer);
			prevLayer = this.layers.get(i);
		}
		// prev layer is now the last layer in the network
		return prevLayer.getOutput();
	}
}
