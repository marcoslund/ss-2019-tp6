from ovito.io import import_file
from ovito.vis import VectorDisplay
from ovito.modifiers import *

node = import_file("ovito_output.xyz", multiple_frames = True, columns = 
    ['Particle Identifier', 'Radius', 'Position.X', 'Position.Y', 'Velocity.X', 'Velocity.Y', 'Pressure'])
# id:I:1:radius:R:1:pos:R:2:velo:R:2:color:R:3
node.add_to_scene()

#cell = node.source.cell
#mat = cell.matrix.copy()
# cell vectors
#mat[0][0] = 0.4 # width
#mat[1][1] = 1.5 # height
# cell origin
#mat[0][3] = 0 # X origin
#mat[1][3] = 0 # Y origin

#cell.matrix = mat

node.modifiers.append(modifier)

node.compute()
