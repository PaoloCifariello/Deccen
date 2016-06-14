# DECCEN
Java implementation of P2P indices computation using [PeerSim](http://peersim.sourceforge.net).

#### What indices?
The goal of DECCEN is to provide a set of algorithms to compute most used centrality indices for nodes of a complex network.
There exists three different indices useful to characterize some properties of a node, given a connected graph G = (V, E):
- Closeness Centrality Index
- Stress Centrality Index
- Betweenness Centrality Index

#### How?
Most current algorithms for the computation of these indices require a global knowledge of the network. The goal of the project is to define and implement DECCEN, a decentralized algorithm for the computation of centrality indices
